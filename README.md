# server
## Description
This is the backend part of the no-code application to make VRDU.
## How to run?
Install the appropriate version Tesseract for your machine from: https://github.com/tesseract-ocr/tesseract 
Update the source code in the DocumentService class to point to the correct tesseract folder on your machine. (currently it is on line 190) And this is the path: src/main/java/com/example/server/service/DocumentService
Get the API key (The JSON version) from the google cloud console and store it somewhere on the computer.
Set an environment variable of the computer to point to the storage point of the downloaded key.
Add the Gemini project id in application.properties 
If you run it, at the same time as the frontend application, they should find each other.
