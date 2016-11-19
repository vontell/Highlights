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
from highlighter2 import Highlighter
from tempfile import tempfile
import logging

_api_key = "AIzaSyBFFC9kGalsfBWbWlX6TqDXqwxflo6s7k0"
YT_OPTS = {
    'format': '17',       
    'outtmpl': tempfile.mkdtemp() + '/vid_%(id)s.3gp',        
    'noplaylist' : True
}

# Given a video id, downlaods the video and returns the filename
def get_video(video_id):
    url = "https://www.youtube.com/watch?v=%s" % (video_id,)
    tempfile = YT_OPTS[outtpl] % (video_id,)
    with youtube_dl.YoutubeDL(YT_OPTS) as ydl
	    ydl.download([url])
    logging.debug("downloaded video from %s to %s" % (url, tempfile))
    return tempfile
	
# Extract the highlights from one video
def get_highlights(filename, model, category):
	hl = Highlighter(filename, model, category).get_highlights()
    logging.debug("generated highlights from %s" % (filename,))

def get_item(id):
	url = 'https://www.googleapis.com/youtube/v3/videos?part=snippet&id='+id+'&key='+_api_key
	data = json.loads(urllib.urlopen(url).read())
    logging.debug("fetched video data from %s" % (url,))
	return data["items"][0]


class Spot:
    def __init__(self, video_id, model=NM_MODEL):
        self.videoId = videoId
        self.snippet get_item(self.videoId)['snippet']
        tempfile = get_video(video_id)
        self.highlights = get_highlights(tempfile, model, self.category())
                        
    def toJson(self):
        return json.dumps(self.__dict__)
        
    def category(self):
        return self.snippet['category']

def load_model(pickename, dataname)
    try :
        bucket = boto3.client('s3').Bucket('highlightmodel')
        with tempfile.TemporaryFile(mode='w+b') as f:
            bucket.Object(pickename).download_fileobj(f)
            model = pickle.load(f)[0]
            logging.info("Loaded model from S3 pickle")
            return model
    except:
    	pass
    
    model_file = os.path.join(tempfile.gettempdir(),pickename)
    try:
    	with open(model_file, 'rb') as f:
    		model = pickle.load(f)[0]
            logging.info("Loaded model from S3 pickle")
            return model
    except:
        pass
    
	data = np.loadtxt(dataname, delimiter=',')
	X = data[:, 0:6]
	y = data[:, 6]
	model = MLPClassifier(
	    activation='logistic', 
	    max_iter=1000, 
	    learning_rate='adaptive', 
	    hidden_layer_sizes=np.full((7, ), 30))
	model.fit(X, y)
	with open(model_file, 'wb') as f:
			pickle.dump([model], f)
	return model


NM_MODEL = load_model('nn_model.pickle', 'data_out.csv')
			
# # Multi-threaded downloading
# titles, urls = get_videos(channel_id)
# print urls
# threads = []
# count = 0
# for title, url in zip(titles, urls):
# 	if count < 5:
# 		thread = Thread(target=extract_scenes, args=("https://www.youtube.com/watch?v="+url, nn_model, title, url))
# 		threads.append(thread)
# 		thread.start()
# 	count += 1
# for thread in threads:
# 	thread.join()

# # Return the json
# with open('out.json', 'w') as outfile:
#     json.dump(videos, outfile)