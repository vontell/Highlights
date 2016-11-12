# Highlights

A product of [YHack '16](http://www.yhack.org/). Built by Aaron Vontell, Ali Benlalah & Cooper Pellaton.

## API

### POST

- `/api/get_subscriptions`

    This requires the client to `POST` a body of the nature below. This will then trigger the endpoint to go and query the YouTube API for the user's subscriptions, and then build a list of the most recent videos which he/she has not seen yet.
    ```json
    {
        "user":"Cooper Pellaton"
    }
    ```

- `/api/get_videos`

    *DEPRECATED*. This endpoint requires the client to `POST` a body similar to that below and then will fetch the user's most recent activity in list form from the YouTube API.
    ```json
    {
        "user":"Cooper Pellaton"
    }
    ```

### GET

-  `/api/fetch_oauth`
    So optimally, what should happen when you call this method is that the user should be prompted to enter her/his Google credentials to authorize the application to then be able to access her/his YouTube account. 
