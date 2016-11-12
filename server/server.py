import logging
import json
import tempfile
import numpy as np
from urllib.request import urlopen
from datetime import datetime

from flask import Flask, jsonify, redirect, url_for, request, Response
from flask_login import *
from pymongo import MongoClient
from bson import json_util

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
    # Sample URL to build
    # https://accounts.google.com/o/oauth2/auth?
    # client_id=1084945748469-eg34imk572gdhu83gj5p0an9fut6urp5.apps.googleusercontent.com&
    # redirect_uri=http%3A%2F%2Flocalhost%2Foauth2callback&
    # scope=https://www.googleapis.com/auth/youtube&
    # response_type=code&
    # access_type=offline
    try:
        json_builder = {
            'client_id': "some_id_needs_to_go_here",
            'redirect_uri': 'http%3A%2F%2Flocalhost%2Fapi%2Foauth2callback',
            'scope': 'https://www.googleapis.com/auth/youtube',
            'response_type': 'code',
            'access_type': 'offline'
        }
        url_to_return = 'https://accounts.google.com/o/oauth2/auth?client_id=' + \
            json_builder['client_id']
        + '&redirect_uri=' + json_builder['redirect_uri']
        + '&scope=' + json_builder['scope']
        + '&response_type=' + json_builder['response_type']
        + '&access_type=' + json_builder['access_type']
        return_json = {
            'url': url_to_return
        }
        response = Response(
            response=return_json, status=200, mimetype='application/json')
    except:
        logging.info("Error: ", sys.exc_info()[0])
        status_info = sys.exc_info()[0]
        resp = Response(
            response=status_info, status=200, mimetype='application/json')
        raise
    resp.headers['Access-Control-Allow-Origin'] = '*'
    return resp


@app.route('/api/oauth2callback', methods=['POST'])
def get_real_token():
    try:
        returned_code = request.args.get('code')
        # Sample to build
        # POST /o/oauth2/token HTTP/1.1
        # Host: accounts.google.com
        # Content-Type: application/x-www-form-urlencoded
        # code=4/ux5gNj-_mIu4DOD_gNZdjX9EtOFf&
        # client_id=1084945748469-eg34imk572gdhu83gj5p0an9fut6urp5.apps.googleusercontent.com&
        # client_secret=hDBmMRhz7eJRsM9Z2q1oFBSe&
        # redirect_uri=http://localhost/oauth2callback&
        # grant_type=authorization_code
        url_json = {
            'code': returned_code,
            'client_id': 'some val from the Google Console',
            'client_secret': 'some other val from the Google Console',
            'redirect_uri': 'http%3A%2F%2Flocalhost%2Fapi%2Frequst_fallback',
            'grant_type': 'authorization_code'
        }
        # Make the request to Google and hopefully get back the legit
        # credentials.
        req = urllib2.Request(
            'https://accounts.google.com/o/oauth2/token',
            url_json, {'Content-Type': 'application/json'})
        f = urllib2.urlopen(req)
        response = f.read()
        db.mvp.insert(response)
        logging.info(response)
        print("Here is the Google response", response)
        f.close()
    # If we got here it means that the user did not grant us access to their
    # YouTube account.
    except:
        logging.info("Error: ", sys.exc_info()[0])
        status_info = sys.exc_info()[0]
        raise


@app.route('/api/get_subscriptions', methods=['POST'])
def get_subscriptions():
    user_info = request.get_json(force=True)
    # Go to the DB and get the user.
    user = db.mvp.find_one(user_info)

    # -------------------------------------
    # Request Subscription data from Google.
    # -------------------------------------
    print(user)

    # Sample URL
    # curl
    # https://www.googleapis.com/youtube/v3/channels?part=id&mine=true&access_token=ACCESS_TOKEN
    fetch_url = 'https://www.googleapis.com/youtube/v3/subscriptions?mine=true&access_token=' + \
        user[access_token]
    req = urllib2.Request(fetch_url)

    # Sample Header
    # Authorization: Bearer ACCESS_TOKEN
    req.add_header('Authorization: Bearer', user[access_token])
    resp = urllib2.urlopen(req)

    # This is a user's subscriptions
    content = resp.read()
    ids = [object["id"] for object in content["items"]]
    channels = np.array([object["channelId"] for object in [object["resourceId"]
                                                            for object in [object["snippet"] for object in content["items"]]]]).flatten()
    channel_urls = get_video_urls(channels)


@app.route('/api/get_videos', methods=['POST'])
def get_videos():
    user_info = request.get_json(force=True)
    # Go to the DB and get the user.
    user = db.mvp.find_one(user_info)

    # -------------------------------------
    # Request Subscription data from Google.
    # -------------------------------------
    print(user)

    # Sample URL
    # curl
    # https://www.googleapis.com/youtube/v3/activities
    fetch_url = 'https://www.googleapis.com/youtube/v3/activities?part=snippet&home=true'
    req = urllib2.Request(fetch_url)

    # Sample Header
    # Authorization: Bearer ACCESS_TOKEN
    req.add_header('Authorization: Bearer', user[access_token])
    resp = urllib2.urlopen(req)

    # This is a user's recommended videos as seen on the home page.
    content = resp.read()
    ids = [object["id"] for object in content["items"]]
    get_video_urls(user, ids)


def get_video_urls(user, ids):
    base_url = 'https://www.googleapis.com/youtube/v3/search'
    urls = []
    for id in ids:
        query_url = base_url + '?part=snippet&' + 'channelID=' + \
            id + '&type=video' + '&order=date&maxResults=1'
        urls.append(query_url)
    get_most_recent_videos(user, urls)


def get_most_recent_videos(user, urls):
    return_json = []
    for url in urls:
        req = urllib2.Request(fetch_url)
        # Sample Header
        # Authorization: Bearer ACCESS_TOKEN
        req.add_header('Authorization: Bearer', user[access_token])
        resp = urllib2.urlopen(req)

        # This is a user's recommended videos as seen on the home page.
        content = resp.read()
        return_json.append(cotent)
    return return_json


def do_the_ml(urls):
    # insert Ali's script here
    pass

if __name__ == "__main__":
    # Run this with python3 server.py and then tail -f mvp.log
    logging.info("Began running at {0}".format(datetime.now()))
    logging.info(" ")
    app.run(host='0.0.0.0', port=3000)
