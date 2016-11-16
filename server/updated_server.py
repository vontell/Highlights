import logging
import httplib2
import json
import os

from datetime import datetime
from furl import furl
from flask import Flask, Response, jsonify, redirect, request, url_for, session
from oauth2client.client import OAuth2WebServerFlow, OAuth2Credentials
from apiclient import discovery

HOSTNAME='vontell-highlights-ironchefpython.c9users.io'

GOOGLE_CLIENT_ID='1029867315898-6fdsb1gkblqmartsudufcmsokkmen388.apps.googleusercontent.com'
YOUTUBE_API_SERVICE_NAME = "youtube"
YOUTUBE_API_VERSION = "v3"
YOUTUBE_SCOPE = "https://www.googleapis.com/auth/youtube"

logging.basicConfig(level=logging.DEBUG)

app = Flask(__name__)
app.secret_key = 'this is not a secret key'
app.config.update(
    SERVER_NAME=HOSTNAME,
    SESSION_TYPE='filesystem'
)

def flow():
    return OAuth2WebServerFlow(
        client_id=GOOGLE_CLIENT_ID,
        client_secret=os.environ['GOOG_SECRET'], 
        scope=YOUTUBE_SCOPE,
        redirect_uri=url_for('oauth2callback', _external=True))

def service():
    return discovery.build(YOUTUBE_API_SERVICE_NAME, YOUTUBE_API_VERSION,
           OAuth2Credentials.from_json(session['credentials']).authorize(
               httplib2.Http()))

@app.before_request
def save_credentials():
    if request.endpoint == 'oauth2callback':
        return
    if 'credentials' not in session or OAuth2Credentials.from_json(
                                session['credentials']).access_token_expired:
        session['url_prior_to_oauth'] = request.path
        return redirect(url_for('oauth2callback'))

@app.route('/oauth2callback')
def oauth2callback():
  if 'code' not in request.args:
    return redirect(flow().step1_get_authorize_url())
  else:
    credentials = flow().step2_exchange(request.args.get('code'))
    session['credentials'] = credentials.to_json()
    return redirect(session['url_prior_to_oauth'])

@app.route('/api/get_subscriptions', methods=['GET'])
def get_subscriptions():
    return json.dumps(service().subscriptions().list(
        part="id,snippet", mine=True, maxResults=50).execute())

if __name__ == "__main__":
    logging.info("Started listening at {0} on http://{1}".format(
        datetime.now(), HOSTNAME))
    app.run(host='0.0.0.0', port=8080)
