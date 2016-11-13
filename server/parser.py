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


def get_ids(str):
	ids = []
	splitted = str.split('"channelId":"')
	for i in range(len(splitted)):
		if i > 0:
			print splitted[i].index('"')
			id = splitted[i][:splitted[i].index('"')]
			ids.append(id)
	return ids


def getVideos(channelID):
	url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&order=date&type=video&videoDuration=medium&channelId="+channelID+"&key=AIzaSyA2cu1skGcRjDfIpG2I1ri_MWeObrZGS30"
	feed1 = urllib.urlopen(url)
	feed1 = feed1.read()
	feed_json1 = json.loads(feed1)
	results = []
	print feed_json1
	for item in feed_json1["items"]:
		results.append("https://www.youtube.com/watch?v="+item['id']["videoId"])
	return results

print getVideos("UCBkNpeyvBO2TdPGVC_PsPUA")