import requests
import logging
import json
import tempfile
import sys
from oauth2client.client import OAuth2WebServerFlow
import numpy as np
import urllib
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
    flow = OAuth2WebServerFlow(client_id='1067255681104-7dltm9n7mvb5v5ghl86p7bh1lc71jo6u.apps.googleusercontent.com',
                               client_secret='TJit9VO6nzvJ03CRgoo3t_4e',
                               scope='https://www.googleapis.com/auth/youtube',
                               redirect_uri='http://li507-39.members.linode.com/api/oauth2callback')
    code = request.args.get('code')
    credentials = flow.step2_exchange(code)
    logging.info("Here is the code: ")
    # url_to_get = 'https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=' + \
    #     code
    # user_json = requests.get(url_to_get)
    # logging.info(user_json)
    # name = user_json['name'] + ' ' + user_json['family_name']
    logging.info(credentials)
    # creds_to_insert = {name: credentials}
    # logging.info(creds_to_insert)
    db.mvp.insert(credentials)

    # try:
    #     returned_code = request.args.get('code')
    #     # Sample to build
    #     # POST /o/oauth2/token HTTP/1.1
    #     # Host: accounts.google.com
    #     # Content-Type: application/x-www-form-urlencoded
    #     # code=4/ux5gNj-_mIu4DOD_gNZdjX9EtOFf&
    #     # client_id=1084945748469-eg34imk572gdhu83gj5p0an9fut6urp5.apps.googleusercontent.com&
    #     # client_secret=hDBmMRhz7eJRsM9Z2q1oFBSe&
    #     # redirect_uri=http://localhost/oauth2callback&
    #     # grant_type=authorization_code
    #     url_json = {
    #         'code': returned_code,
    #         'client_id': '1067255681104-7dltm9n7mvb5v5ghl86p7bh1lc71jo6u.apps.googleusercontent.com',
    #         'client_secret': 'TJit9VO6nzvJ03CRgoo3t_4e',
    #         'redirect_uri': 'http%3A%2F%2Fli507-39.members.linode.com%2Fapi%2Frequst_fallback',
    #         'grant_type': 'authorization_code'
    #     }
    #     flow = OAuth2WebServerFlow(client_id='1067255681104-7dltm9n7mvb5v5ghl86p7bh1lc71jo6u.apps.googleusercontent.com',
    #                                client_secret='TJit9VO6nzvJ03CRgoo3t_4e',
    #                                scope='https://www.googleapis.com/auth/youtube',
    #                                redirect_uri='http%3A%2F%2Fli507-39.members.linode.com%2Fapi%2Frequst_fallback')

    #     # Make the request to Google and hopefully get back the legit
    #     # credentials.
    #     response = requests.post('https://accounts.google.com/o/oauth2/token',
    #                              data=url_json, headers={'Content-Type': 'application/json'})
    #     logging.info("Here is the Google response")
    #     logging.info(response)
    #     db.mvp.insert(response)
    #     logging.info(response)
    # # If we got here it means that the user did not grant us access to their
    # # YouTube account.
    # except:
    #     logging.info("Error: ", sys.exc_info()[0])
    #     status_info = sys.exc_info()[0]
    #     raise


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
        str(user[access_token])
    req = urllib.request(fetch_url)

    # Sample Header
    # Authorization: Bearer ACCESS_TOKEN
    req.add_header('Authorization: Bearer', user[access_token])
    resp = urllib.urlopen(req)

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
            str(id) + '&type=video' + '&order=date&maxResults=1'
        urls.append(query_url)
    get_most_recent_videos(user, urls)


def get_most_recent_videos(user, urls):
    return_json = []
    for url in urls:
        req = urllib2.Request(fetch_url)
        # Sample Header
        # Authorization: Bearer ACCESS_TOKEN
        req.add_header('Authorization: Bearer', user[access_token])
        resp = urllib.urlopen(req)

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
    app.run(host='0.0.0.0', port=80)
