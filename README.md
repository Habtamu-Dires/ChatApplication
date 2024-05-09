API Documentation

Welcome to the API documentation for the One-to-One Chat application. This API facilitates communication between users via text messages and supports functionalities such as updating user profiles, sending messages, attaching media files, and retrieving chat history.
Base URL

The base URL for all API endpoints is: https://localhost:8080
Authentication

Authentication (Basic Authentication) is required for all endpoints except registration endpoint. Please include username and password to access each endpoint. 
Endpoints
1. User Management
1.1 Update User Profile

    Endpoint: PUT /api/v1/users/profile-update
    Description: Update user profile information.
    Request Body Example:

    json
{
  "phoneNumber": "string",
  "firstName": "string",
  "lastName": "string",
  "username": "string",
  "password": "string"
}

Success Response (200 OK) Example:

json

    {
      "phoneNumber": "string",
      "firstName": "string",
      "lastName": "string",
      "username": "string",
      "password": "string"
    }

1.2 Get User Profile

    Endpoint: GET /api/v1/users/profile
    Description: Retrieve user profile information.
    Success Response (200 OK) Example:

    json

    {
      "phoneNumber": "string",
      "firstName": "string",
      "lastName": "string",
      "username": "string",
      "password": "string"
    }

1.3 User Registration

    Endpoint: POST /api/v1/auth/signup
    Description: Register a new user.
    Request Body Example:

    json

{
  "phoneNumber": "string",
  "firstName": "string",
  "lastName": "string",
  "username": "string",
  "password": "string"
}

Success Response (200 OK) Example:

json

    {
      "username": "string"
    }

2. Messaging
2.1 Send Message

    Endpoint: POST /api/v1/chat/send
    Description: Send a text message.
    Request Body Example:

    json

{
  "sender": "string",
  "recipient": "string",
  "text": "string",
  "attachmentType": "string",
  "attachmentPath": "string"
}

Success Response (200 OK) Example:

json

    "success"

2.2 Get Messages

    Endpoint: GET /api/v1/chat/messages/{sender}/{recipient}
    Description: Get messages between two users.
    Success Response (200 OK) Example:

    json

    [
      {
        "sender": "string",
        "recipient": "string",
        "text": "string",
        "attachmentType": "string",
        "attachmentPath": "string"
      }
    ]

3. Attachments
3.1 Upload Video

    Endpoint: POST /api/v1/attachments/uploadVideo
    Description: Upload a video file.
    Request Body Example:

    json

{
  "file": "string"
}

Success Response (200 OK) Example:

json

    "success"

3.2 Upload Picture

    Endpoint: POST /api/v1/attachments/uploadPicture
    Description: Upload a picture file.
    Request Body Example:

    json

{
  "file": "string"
}

Success Response (200 OK) Example:

json

    "success"

4. Chat Responses
4.1 Get Chat Responses

    Endpoint: GET /api/v1/chat-response/{chatMessageId}
    Description: Get chat responses (emojis) for a message.
    Success Response (200 OK) Example:

    json

[
  {
    "id": 0,
    "emoji": "THUMBUP",
    "count": 0,
    "chatMessage": {
      "id": 0,
      "chatId": "string",
      "sender": {
        "id": 0,
        "username": "string",
        "phoneNumber": "string",
        "firstName": "string",
        "lastName": "string",
        "password": "string",
        "about": "string",
        "lastSeen": "string"
      },
      "recipient": {
        "id": 0,
        "username": "string",
        "phoneNumber": "string",
        "firstName": "string",
        "lastName": "string",
        "password": "string",
        "about": "string",
        "lastSeen": "string"
      },
      "text": "string",
      "attachmentType": "string",
      "attachmentPath": "string",
      "timestamp": "2024-05-08T14:05:01.552Z"
    }
  }
]
