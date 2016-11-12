import requests
import logging
import httplib2
import json
import tempfile
import pickle
import sys
import oauth2client
from oauth2client.client import OAuth2WebServerFlow, Storage
import numpy as np
import urllib
import pprint
from urllib.request import urlopen
from datetime import datetime

from flask import Flask, jsonify, redirect, url_for, request, Response
from flask_login import *
from pymongo import MongoClient
from bson import json_util

# Initialize a storage object
storage = Storage('a_credentials_file')

# Single user auth credentials
http = httplib2.Http()

# Flask configuration
app = Flask(__name__)

# Setup logging
logging.basicConfig(filename='mvp.log', level=logging.DEBUG)

# Setup a DB incase we need this to store YouTube creds.
database = "localhost:27017"
client = MongoClient(database)
db = client.beta

# Build a URL that will query Google for an auth token. Then return this
# URL to the client to get the token.


@app.route('/api/fetch_oauth', methods=["GET"])
def get_oauth_token():
    logging.info("Recieved a request to process OAUTH")
    # Sample URL to build
    # https://accounts.google.com/o/oauth2/auth?
    # client_id=1084945748469-eg34imk572gdhu83gj5p0an9fut6urp5.apps.googleusercontent.com&
    # redirect_uri=http%3A%2F%2Flocalhost%2Foauth2callback&
    # scope=https://www.googleapis.com/auth/youtube&
    # response_type=code&
    # access_type=offline
    flow = OAuth2WebServerFlow(client_id='1067255681104-7dltm9n7mvb5v5ghl86p7bh1lc71jo6u.apps.googleusercontent.com',
                               client_secret='TJit9VO6nzvJ03CRgoo3t_4e',
                               scope='https://www.googleapis.com/auth/youtube https://www.googleapis.com/auth/userinfo.email',
                               redirect_uri='http://li507-39.members.linode.com/api/oauth2callback')
    auth_uri = flow.step1_get_authorize_url()
    return redirect(auth_uri)


@app.route('/api/oauth2callback', methods=['GET', 'POST'])
def get_real_token():
    credentials = None
    try:
        with open('credentials.pickle', 'rb') as f:
            credentials = pickle.load(f)[0]
    except FileNotFoundError:
        flow = OAuth2WebServerFlow(client_id='1067255681104-7dltm9n7mvb5v5ghl86p7bh1lc71jo6u.apps.googleusercontent.com',
                                   client_secret='TJit9VO6nzvJ03CRgoo3t_4e',
                                   scope='https://www.googleapis.com/auth/youtube',
                                   redirect_uri='http://li507-39.members.linode.com/api/oauth2callback')
        code = request.args.get('code')
        credentials = flow.step2_exchange(code)
        with open('credentials.pickle', 'wb') as f:
            pickle.dump([credentials], f)
        # storage.put(flow.step2_exchange(code))
        # credentials.refresh(http)
        user_video_data = get_subscriptions()
        logging.info(user_video_data)
        return user_video_data


@app.route('/api/get_subscriptions', methods=['GET'])
def get_subscriptions():

    # -------------------------------------
    # Request Subscription data from Google.
    # -------------------------------------

    # Sample URL
    # curl
    # https://www.googleapis.com/youtube/v3/channels?part=id&mine=true&access_token=ACCESS_TOKEN
    fetch_url = 'https://www.googleapis.com/youtube/v3/subscriptions?mine=true&part=snippet&prettyPrint=false'
    credentials = None
    with open('credentials.pickle', 'rb') as f:
        credentials = pickle.load(f)[0]
    http1 = credentials.authorize(http)
    headers, content = http1.request(fetch_url, method="GET")
    result = str(content)[str(content).index(
        "{"): len(str(content)) - str(content)[::-1].index("}")]
    result = result.replace('\\\"', "")
    # logging.info(result)
    # logging.info(str(content))

    # This is a user's subscriptions
    with open('blob.pickle', 'wb') as f:
        pickle.dump([content], f)
    # logging.info(json.d(content))
    # results = json.loads(result)
    ids = get_ids(result)
    logging.info(ids)
    return 'true'


def get_ids(str):
    ids = []
    splitted = str.split('"channelId":"')
    for i in range(len(splitted)):
        if i > 0:
            logging.info(splitted[i].index('"'))
            id = splitted[i][:splitted[i].index('"')]
            ids.append(id)
    return ids


@app.route('/api/get_videos', methods=['POST'])
def get_videos():
    user_info = request.get_json(force=True)
    # Go to the DB and get the user.
    user = db.mvp.find_one(user_info)

    # --------------------------------------
    # Request Subscription data from Google.
    # --------------------------------------
    print(user)

    # Sample URL
    # curl
    # https://www.googleapis.com/youtube/v3/activities
    fetch_url = 'https://www.googleapis.com/youtube/v3/activities?part=snippet&home=true'
    req = urllib2.Request(fetch_url)

    # Sample Header
    # Authorization: Bearer ACCESS_TOKEN
    req.add_header('Authorization: Bearer', user[access_token])
    resp = urllib.urlopen(req)

    # This is a user's recommended videos as seen on the home page.
    content = resp.read()
    ids = [object["id"] for object in content["items"]]
    get_video_urls(user, ids)


def get_video_urls(user, ids):
    base_url = 'https://www.googleapis.com/youtube/v3/search'
    urls = []
    for id in ids:
        query_url = base_url + '?part=snippet&' + 'channelID=' + \
            str(id) + '&type=video' + '&order=date&maxResults=20'
        urls.append(query_url)
    get_most_recent_videos(user, urls)


def get_most_recent_videos(user, urls):
    return_json = []
    credentials = storage.get()
    http = credentials.authorize(http)
    for url in urls:
        req = http.request(url, "GET")
        # req = urllib2.Request(fetch_url)
        # # Sample Header
        # # Authorization: Bearer ACCESS_TOKEN
        # req.add_header('Authorization: Bearer', user[access_token])
        # resp = urllib.urlopen(req)

        # This is a user's recommended videos as seen on the home page.
        return_json.append(content)
    return return_json


def do_the_ml(urls):
    # insert Ali's script here
    pass

if __name__ == "__main__":
    # Run this with python3 server.py and then tail -f mvp.log
    logging.info("Began running at {0}".format(datetime.now()))
    logging.info(" ")
    app.run(host='0.0.0.0', port=80)
