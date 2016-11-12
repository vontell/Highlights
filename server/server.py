import logging
import json
import tempfile
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
            'redirect_uri': 'http%3A%2F%2Flocalhost%2Fapi%2Frequst_fallback'
            'grant_type': 'authorization_code'
        }
        # request_url =
        # Post to https://accounts.google.com/o/oauth2/token with the above
        # JSON and then store the response


if __name__ == "__main__":
    logging.info("Began running at {0}".format(datetime.now()))
    logging.info(" ")
    app.run(host='0.0.0.0', port=3000)
