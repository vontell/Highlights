import cv2 as cv2
from threading import Thread, Lock
from Queue import Queue
import sys
import numpy as np
import time, math

# Parameter of the program
# _skip_rate = 4				# Number of frame to skip per frame
# _min_array_length = 150		# Depth when parallelising the program
# _min_scene_length = 6 		# The minimum number of seconds in a scene
# _max_scene_length = 30 		# The max number of seconds in a scene
# _alpha = 0.05 				# The weight for expaverage the scene detection

class Highlighter:

	def __init__(self, fname, model, category, _skip_rate=4, _max_array_ratio=0.27, _min_scene_length = 6, _max_scene_length=30, _alpha = 0.05):
		self.cap = cv2.VideoCapture(fname)
		self.length = int(self.cap.get(cv2.cv.CV_CAP_PROP_FRAME_COUNT))
		self.lock = Lock()
		self._min_array_length = int(self.length*_max_array_ratio)
		self._skip_rate = _skip_rate
		self._min_scene_length = _min_scene_length
		self._max_scene_length = _max_scene_length
		self._alpha = _alpha
		self.model = model
		self.category = category
		self.fps = self.cap.get(cv2.cv.CV_CAP_PROP_FPS)
		self.fps = 30 if(math.isnan(self.fps)) else self.fps

	def highlight_part(self, cap, start, end, queue):
		# Check If we reached bottom of recursion
		if end-start < self._min_array_length:
			queue.put(self.conquer(cap, start, end))
			return

		# Divide part
		queuer = Queue()
		queuel = Queue()
		q = (start+end)/2
		threadl = Thread(target=self.highlight_part, args=(cap, start, q, queuel))
		threadr = Thread(target=self.highlight_part, args=(cap, q, end, queuer))
		threadl.start()
		threadr.start()

		# Waiting for threads to join
		threadl.join()
		threadr.join()

		# Getting back the results
		hl, lff, llf = queuel.get()
		hr, rff, rlf = queuer.get()

		# Combine part
		if self.combine(llf, rff, hl[-1]["avrg_bspeed"]):
			hl[-1]["end_frame"] = hl[0]["end_frame"]
			hl[-1]["end"] = hl[0]["end"]
			hl[-1]["duration"] += hl[0]["duration"]
			hl[-1]["max_bspeed"] = max(hl[-1]["max_bspeed"], hl[0]["max_bspeed"])
			hl[-1]["max_brightness"] = max(hl[-1]["max_brightness"], hl[0]["max_brightness"])
			hl[-1]["avrg_brightness"] = (hl[-1]["avrg_brightness"] + hl[0]["avrg_brightness"])/2.
			hl[-1]["avrg_bspeed"] = (hl[-1]["avrg_bspeed"] + hl[0]["avrg_bspeed"])/2.
			del hr[0]

		# Filtering the good scenes
		ret_scenes = []
		for j in range(len(hl)):
			if j == 0 or (hl[j]["duration"] > self._min_scene_length and hl[j]["duration"] < self._max_scene_length):
				ret_scenes.append(hl[j])
		for j in range(len(hr)):
			if  (j == len(hr) - 1) or (hr[j]["duration"] > self._min_scene_length and hr[j]["duration"] < self._max_scene_length):
				ret_scenes.append(hr[j])

		# Put the result in the return queue
		queue.put((ret_scenes, lff, rlf))

	def combine(self, frame1, frame2, exp_avrg):
		fgbg = cv2.BackgroundSubtractorMOG()
		fgbg.apply(frame1)
		fgmask = fgbg.apply(frame2)
		w,h = fgmask.shape
		diff = np.count_nonzero(fgmask)/(1.0*w*h)
		return (diff > exp_avrg)

	def conquer(self, cap, start, end):
		# Background Subtraction parameters
		i = 0
		fgbg = cv2.BackgroundSubtractorMOG()

		# Resutls
		scenes = []

		# Scene feature initialization
		exp_avrg = 0
		max_bspeed = 0
		max_brightness = 0
		avrg_brightness = 0

		# Iterating over the frames
		last_frame = []
		first_frame = []
		current_frame_start = start
		currentframe = start
		while currentframe < end:
			# Locking the capture to get the frame
			self.lock.acquire()
			cap.set(cv2.cv.CV_CAP_PROP_POS_FRAMES, currentframe-1)
			ret, frame = cap.read()
			self.lock.release()

			if ret:
				# Save the first frame
				if len(first_frame) == 0:
					first_frame = frame

				# Apply the background subtraction
				if i == 2:
					fgbg = cv2.BackgroundSubtractorMOG()
					i = 0
				fgmask = fgbg.apply(frame)
				frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
				width, height = frame.shape

				# Get the brightness
				brightness = frame.mean()
				if i > 0 and i < 2:
					w,h = fgmask.shape
					diff = np.count_nonzero(fgmask)/(1.0*w*h)

					# Add new scene if big difference encountered
					if diff > exp_avrg or (currentframe+self._skip_rate+1) >= end:
						if current_frame_start != currentframe:
							avrg_brightness /= 1.0*(currentframe-current_frame_start)/(self._skip_rate+1)
						scenes.append({"end_frame": currentframe, 
									   "max_bspeed": max_bspeed,
									   "avrg_bspeed": exp_avrg,
									   "max_brightness": max_brightness,
									   "avrg_brightness": avrg_brightness})

						# Reinitialize the scene fetures
						exp_avrg =  self._alpha*diff+(1-self._alpha)*exp_avrg
						max_bspeed = 0
						max_brightness = 0
						avrg_brightness = 0
						last_frame = frame

					# Compute the frame features
					max_bspeed = max(diff, max_bspeed)
				
				# Compute the frame features
				max_brightness = max(brightness, max_brightness)
				avrg_brightness += brightness
				i += 1
			
			# Increment the frame index & skipping some
			currentframe += self._skip_rate + 1
		
		# Save the scenes
		fr_start = 0
		ret_scenes = []
		for j in range(len(scenes)):
			scene = scenes[j]
			fr_end = scene["end_frame"]
			duration = (fr_end-fr_start)
			if duration > self._skip_rate:
				scenes[j]["start_frame"] = fr_start
				scenes[j]["start"] = fr_start/self.fps
				scenes[j]["end"] = fr_end/self.fps
				scenes[j]["duration"] = duration/self.fps
				scenes[j]["position"] = fr_start/self.length
				ret_scenes.append(scenes[j])
			fr_start = fr_end

		# Return the result in the queue
		return (ret_scenes, first_frame, last_frame)


	def get_highlights(self):
		start = time.time()
		q = Queue()
		self.highlight_part(self.cap, 0, self.length, q)
		hl, l, r = q.get()

		# Clean the last and first scenes
		if len(hl) > 0:
			if hl[0]["duration"] <= self._min_scene_length or hl[0]["duration"] >= self._max_scene_length:
				del hl[0]
		if len(hl) > 0:
			if hl[-1]["duration"] <= self._min_scene_length or hl[-1]["duration"] >= self._max_scene_length:
				del hl[-1]

		# Stick scenes together if difference less that 3 secs
		# last_scene_end = 0
		# i = 0
		# while i < len(hl):
		# 	if i > 0:
		# 		if hl[i]["start"]-last_scene_end <= 3:
		# 			hl[i-1]["end_frame"] = hl[i]["end_frame"]
		# 			hl[i-1]["end"] = hl[i]["end"]
		# 			hl[i-1]["duration"] += hl[i]["duration"]
		# 			hl[i-1]["max_bspeed"] = max(hl[i-1]["max_bspeed"], hl[i]["max_bspeed"])
		# 			hl[i-1]["max_brightness"] = max(hl[i-1]["max_brightness"], hl[i]["max_brightness"])
		# 			hl[i-1]["avrg_brightness"] = (hl[i-1]["avrg_brightness"] + hl[i]["avrg_brightness"])/2.
		# 			hl[i-1]["avrg_bspeed"] = (hl[i-1]["avrg_bspeed"] + hl[i]["avrg_bspeed"])/2.
		# 			last_scene_end = hl[i]["end"]
		# 			del hl[i]
		# 			i -= 1
		# 		else:
		# 			last_scene_end = hl[i]["end"]
		# 	else:
		# 		last_scene_end = hl[i]["end"]
		# 	i += 1

		# Feed the senes to the ML
		i = 0
		avrg_duration = 0
		for scene in hl:
			vect = [
					float(self.category),
					scene["avrg_brightness"],
					scene["avrg_bspeed"],
					scene["duration"],
					scene["max_brightness"],
					scene["max_bspeed"]
				]
			vect = np.array(vect)
			avrg_duration += scene["duration"]
			#hl[i]["score"] = np.asscalar(self.model.predict(vect.reshape(1,-1)))
			i += 1

		if len(hl) > 0:
			avrg_duration /= 1.0*len(hl)
		if avrg_duration > (self._max_scene_length+self._min_scene_length)/2:
			hl.sort(key=lambda x: (-x['duration']))
		else:
			hl.sort(key=lambda x: (x['start']))

		d = 0
		i = 0
		while i < len(hl):
			if d > self._max_scene_length:
				del hl[i]
			else:
				d += hl[i]['duration']
			i += 1

		end = time.time()
		self.last_time = end - start
		return hl

	def get_time(self):
		return self.last_time