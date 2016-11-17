import logging
import httplib2
import os

from datetime import datetime
from furl import furl
from flask import Flask, Response, jsonify, redirect, request, url_for, session, send_from_directory
from oauth2client.client import OAuth2WebServerFlow, OAuth2Credentials
from apiclient import discovery
from flask_formatter import Formatter

# testing9499924@gmail.com

HOSTNAME='vontell-highlights-ironchefpython.c9users.io'

GOOGLE_CLIENT_ID='1029867315898-6fdsb1gkblqmartsudufcmsokkmen388.apps.googleusercontent.com'
YOUTUBE_API_SERVICE_NAME = "youtube"
YOUTUBE_API_VERSION = "v3"
YOUTUBE_SCOPE = "https://www.googleapis.com/auth/youtube"

logging.basicConfig(level=logging.DEBUG)

app = Flask(__name__, static_url_path='/static')
app.response_class = Formatter
app.secret_key = 'this is not a secret key'
app.config.update(
    SERVER_NAME=HOSTNAME,
    SESSION_TYPE='filesystem'
)

@Formatter.of(list, dict)
def format_json(obj):
    return jsonify(obj)

@app.before_first_request
def define_flow():
    global FLOW; FLOW = OAuth2WebServerFlow(
        client_id=GOOGLE_CLIENT_ID,
        client_secret=os.environ['GOOG_SECRET'], 
        scope=YOUTUBE_SCOPE,
        redirect_uri=url_for('oauth2callback', _external=True))

def service():
    return discovery.build(YOUTUBE_API_SERVICE_NAME, YOUTUBE_API_VERSION,
           credentials().authorize(httplib2.Http()))

def credentials():
    return OAuth2Credentials.from_json(session['credentials'])
    
@app.before_request
def save_credentials():
    if request.endpoint != 'oauth2callback':
        if 'credentials' not in session or credentials().access_token_expired:
            session['url_prior_to_oauth'] = request.path
            return redirect(url_for('oauth2callback'))

@app.route('/oauth2callback')
def oauth2callback():
  if 'code' not in request.args:
    return redirect(FLOW.step1_get_authorize_url())
  else:
    credentials = FLOW.step2_exchange(request.args.get('code'))
    session['credentials'] = credentials.to_json()
    return redirect(session['url_prior_to_oauth'])

@app.route('/api/subscriptions')
def get_subscriptions():
    return service().subscriptions().list(
        part="id,snippet", mine=True, maxResults=50).execute()

@app.route('/api/channels/<channel_id>/videos', methods=['GET'])
def get_videos_for_channel(channel_id):
    return service().search().list(part="snippet", type="video",
            channelId=channel_id, order="date", maxResults=50).execute()

@app.route('/api/subscribed_videos')
def get_videos():
    channel_ids = [sub['snippet']['resourceId']['channelId'] 
                                    for sub in get_subscriptions()['items']]
    search_results = map(get_videos_for_channel, channel_ids)
    return sum([res['items'] for res in search_results], [])

@app.route("/")
def index():
     return redirect(url_for('static', filename='index.html'))

if __name__ == "__main__":
    logging.info("Started listening at {0} on http://{1}".format(
        datetime.now(), HOSTNAME))
    app.run(host='0.0.0.0', port=8080)
