import sys,os, shutil
import youtube_dl
from pytube import YouTube
import numpy as np
import cv2 as cv2
import urllib
import json, uuid
import isodate
import pickle
from threading import Thread
import warnings
from sklearn.neural_network import MLPClassifier
from sklearn.svm import SVC
import urlparse

#Program Constants
_min_scene_length = 6 # the minimum number of seconds in a scene
_min_min_scene_length = 4
_max_scene_length = 30 # the max number of seconds in a scene
_alpha = 0.05 # the wight for the scene detection

# Disable warnings
def fxn():
    warnings.warn("deprecated", DeprecationWarning)
with warnings.catch_warnings():
    warnings.simplefilter("ignore")
    fxn()

# Parsing the urls passed as argument
channel_id = sys.argv[1]
videos = []

# Extract the scenes by applying the pipeline
def extract_scenes(url, model, title, video_id):
	print "START"
	fname, cat = get_video(url)
	print "END"
	#print "DONE DOWNL"
	cap = cv2.VideoCapture(fname)
	# cat = 2
 	# cap = cv2.VideoCapture('video.3gp')
	#print directory+'/'+fname+'.3gp'
	scenes = get_scenes(cap)
	#print "GOT SCENES"
	i = 0
	avrg_duration = 0
	for scene in scenes:
		vect = [
				float(cat),
				scene["avrg_brightness"],
				scene["avrg_bspeed"],
				scene["duration"],
				scene["max_brightness"],
				scene["max_bspeed"]
			]
		vect = np.array(vect)
		avrg_duration += scene["duration"]
		scenes[i]["score"] = np.asscalar(model.predict(vect.reshape(1,-1)))
		i += 1

	if len(scenes) > 0:
		avrg_duration /= 1.0*len(scenes)
	if avrg_duration > (_max_scene_length+_min_scene_length)/2:
		scenes.sort(key=lambda x: (-x['score'], -x['duration']))
	else:
		scenes.sort(key=lambda x: (x['start']))

	d = 0
	ret_scenes = {"title": title, "video_id": video_id, "url": url, "highlights": []}
	for i in range(len(scenes)):
		if d > _max_scene_length:
			break
		ret_scenes["highlights"].append(scenes[i])
		d += scenes[i]['duration']
	videos.append(ret_scenes)


# Download the video in the directory
def get_video(yt_url):
	ydl_opts = {
	    'format': '160',       
	    'outtmpl': '%(id)s',        
	    'noplaylist' : True
	}
	with youtube_dl.YoutubeDL(ydl_opts) as ydl:
		ydl.download([yt_url])

	url_data = urlparse.urlparse(yt_url)
	query = urlparse.parse_qs(url_data.query)
	video_id = query["v"][0]

	cat = get_category(video_id)
	return (video_id, cat)

# Get the category of a video given an id
def get_category(id):
	# Get category
	urlCat = 'https://www.googleapis.com/youtube/v3/videos?part=snippet&id='+id+'&key=AIzaSyA2cu1skGcRjDfIpG2I1ri_MWeObrZGS30'
	feed1 = urllib.urlopen(urlCat)
	feed1 = feed1.read()
	feed_json1 = json.loads(feed1)
	cat = feed_json1["items"][0]['snippet']['categoryId']
	return cat

# Get the videos from the channel ID
def get_videos(channelID):
	url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&order=date&type=video&videoDuration=medium&channelId="+channelID+"&key=AIzaSyA2cu1skGcRjDfIpG2I1ri_MWeObrZGS30"
	feed1 = urllib.urlopen(url)
	feed1 = feed1.read()
	feed_json1 = json.loads(feed1)
	results = []
	titles = []
	for item in feed_json1["items"]:
		titles.append(item["snippet"]["title"])
		results.append(item['id']["videoId"])
	return titles, results

# Extract the scene from the video once downloaded
def get_scenes(cap, tail=2):
	last_frame = None
	i = 0
	fgbg = cv2.BackgroundSubtractorMOG()
	exp_avrg = 0
	scenes = []
	avrgs = []
	diffs = []
	max_bspeed = 0
	count = 0
	max_brightness = 0
	avrg_brightness = 0
	while(cap.isOpened()):
		ret, frame = cap.read()
		if ret:
			if i == tail:
				fgbg = cv2.BackgroundSubtractorMOG()
				i = 0
			fgmask = fgbg.apply(frame)
			frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
			brightness = frame.mean()
			width, height = frame.shape
			if i > 0 and i < tail:
				w,h = fgmask.shape
				diff = np.count_nonzero(fgmask)/(1.0*w*h)
				if diff > exp_avrg:
					avrg_brightness /= 1.0*count
					scenes.append({"end_frame": cap.get(cv2.cv.CV_CAP_PROP_POS_FRAMES), 
								   "max_bspeed": max_bspeed,
								   "avrg_bspeed": exp_avrg,
								   "max_brightness": max_brightness,
								   "avrg_brightness": avrg_brightness})
					exp_avrg =  _alpha*diff+(1-_alpha)*exp_avrg
					max_bspeed = 0
					max_brightness = 0
				
				max_bspeed = max(diff, max_bspeed)
				avrgs.append(exp_avrg)
				diffs.append(diff)
			count += 1
			max_brightness = max(brightness, max_brightness)
			avrg_brightness += brightness

			i += 1
			if cap.get(cv2.cv.CV_CAP_PROP_POS_FRAMES) == cap.get(cv2.cv.CV_CAP_PROP_FRAME_COUNT):
				break
	#print scenes
	fr_start = 0
	ret_scenes = []
	for j in range(len(scenes)):
		scene = scenes[j]
		fr_end = scene["end_frame"]
		duration = (fr_end-fr_start)/cap.get(cv2.cv.CV_CAP_PROP_FPS)
		if duration > _min_scene_length and duration < _max_scene_length:
			scenes[j]["start"] = fr_start/cap.get(cv2.cv.CV_CAP_PROP_FPS)
			scenes[j]["end"] = fr_end/cap.get(cv2.cv.CV_CAP_PROP_FPS)
			scenes[j]["duration"] = duration
			scenes[j]["position"] = fr_start/cap.get(cv2.cv.CV_CAP_PROP_FRAME_COUNT)
			ret_scenes.append(scenes[j])
		fr_start = fr_end

	return ret_scenes


#print urls
# Loading the ML model
#print "Loading the model..."
nn_model = None
try:
	with open('nn_model.pickle', 'rb') as f:
		nn_model = pickle.load(f)[0]
		#print "Model loaded."
except:
	pass

if nn_model == None:
	data = np.loadtxt("data_out.csv", delimiter=',')
	X = data[:, 0:6]
	y = data[:, 6]
	classifier = MLPClassifier(activation='logistic', max_iter=1000, learning_rate='adaptive', hidden_layer_sizes=np.full((7, ), 30))
	classifier.fit(X, y)
	nn_model = classifier
	with open('nn_model.pickle', 'wb') as f:
			pickle.dump([classifier], f)
			
# Multi-threaded downloading
titles, urls = get_videos(channel_id)
print "GOT videos"
print urls
threads = []
count = 0
for title, url in zip(titles, urls):
	if count < 5:
		thread = Thread(target=extract_scenes, args=("https://www.youtube.com/watch?v="+url, nn_model, title, url))
		threads.append(thread)
		thread.start()
	count += 1
for thread in threads:
	thread.join()

# Return the json
with open('out.json', 'w') as outfile:
    json.dump(videos, outfile)
