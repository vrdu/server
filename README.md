# server
## Description
This is the backend part of the no-code application. It handles the requests comeing from the frontend, stores the necessary data in the database, handles the OCR as well as the extraction using Gemini.
## How to run?
Install the appropriate version Tesseract for your machine from: https://github.com/tesseract-ocr/tesseract  
Update the source code in the DocumentService class to point to the correct tesseract folder on your machine. (currently it is on line 190) And this is the path: src/main/java/com/example/server/service/DocumentService   
Get the API key (The JSON version) from the google cloud console and store it somewhere on the computer.   
Set an environment variable of the computer to point to the storage point of the downloaded key.   
Add the Gemini project id in application.properties   
If you run it, at the same time as the frontend application, they should find each other.   

## High Level description of the different components
The application consists of the following components, which are graphically presented in the picture below:

- **Front End:**  
  This component is responsible for user interactions. It includes all visual elements and their corresponding functionalities. User input is transmitted to the backend via a REST interface.

- **Controller:**  
  The controller manages interactions with external components. The application includes multiple controller classes, each responsible for a specific domain. For example, the `UserController` handles data related to users, while the `LLMController` manages communication with the Gemini API.

- **Mapper:**  
  The mapper facilitates data conversion between the frontend and internal entities. It ensures seamless transformation of data representations shared with the frontend into internal formats and vice versa.

- **Service:**  
  Service classes provide specialized functionality for distinct parts of the application. For instance, the `UserService` implements logic for creating, retrieving, updating users, and generating and validating tokens. These classes operate on the internal entities and encapsulate core application logic.

- **Repository:**  
  Repositories are interfaces provided by Spring Boot to abstract database interactions. They support CRUD (Create, Read, Update, Delete) operations, enabling efficient data management. Each database table is associated with a dedicated repository to ensure modular and maintainable interactions with the data layer.
  ![Application Architecture](architecture.png "Architecture Overview")


