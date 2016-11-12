from __future__ import unicode_literals
import youtube_dl, uuid

def my_hook(d):
    if d['status'] == 'finished':
        print('Done downloading, now converting ...')

ydl_opts = {
    'format': '160',       
    'outtmpl': '%(id)s',        
    'noplaylist' : True,
    'output': str(uuid.uuid4())+".mp4"
}

with youtube_dl.YoutubeDL(ydl_opts) as ydl:
    print ydl.download(['https://www.youtube.com/watch?v=pwp1CH5R-w4'])
