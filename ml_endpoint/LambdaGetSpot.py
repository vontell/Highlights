import boto3
from spot import Spot

def respond(err, res=None):
    return {
        'statusCode': '400' if err else '200',
        'body': err.message if err else res,
        'headers': {
            'Content-Type': 'application/json',
        },
    }

def lambda_handler(event, context):
    dynamo = boto3.resource('dynamodb').Table('Spotlights')
    videoId = str(event["videoId"])
    response = dynamo.get_item(Key={'videoId': videoId})
    if ('Item' not in response):
        spot = {
            "videoId": videoId,
            "data": Spot(videoId).toJson
        }
        dynamo.put_item(Item=spot)
    else:
        spot = response['Item']

    return spot['data']
