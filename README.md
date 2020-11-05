# PsychApp
The Android application 'PsychApp' that works as a Digital Questionnaire useful for any Psychology research.

Description:
    Answer questions in specific times throughout the day. The user can set theese times. The questions are specific to each project. Settings regarding the study or user are retrieved from the database. The answers given by the user are also sent to this database. The app also works in offline mode by storing the answers project settings localy, in this case the answers are sent when a login/authentication attempt occurs (with an established internet connection).
    An authentication code is needed to access the application, This code should be generated and given by the project owner.  


Project - A research project owned/run by a researcher. The app is used to gather information by multiple users. The information gathered is in the form of a questionnaire. The project contains some defining information, like: the researchers, a description, the number of days and times per day an answer should be requested, a set of questions, and a user list.

Researcher - Is an owner of a project or works for one. He/She can view the answers of any user in his project while creating new user accounts. He can alter the project settings and/or information. (Super researchers can also create/delete projects and add/remove researchers from projects)

User - He uses this mobile application to answer the set of questions the required amount of times per day until a required amount of days passes.

Question - Questions can have multiple types/formats. 
  > Simple text format: requires the user to write an answer
  > Multiple choice: the user must provide an answer from the existing options. (These questions may allow the user to write his own answer if set to do so). Multiple choice questions can either be displayed in vertical or horizontal order
  > Slider questions: the answer is given in the form of a slider, or rather, a percentage represented by a slider.
  
Answers - It is important that for each answer the question it answers, tne time it was issued, and (obviously) the actual answer is known. Each answer belongs to a specific user.


App features:
  - Notifies user when its time to answer the digital questionnaire
  - Shows the user a set of questions which the user should answer. These answers are sent to a designated server.
    The application can store the answers if connection to the internet is not established so that these aswers can later
    be sent to the database.
  - Present some useful research information regarding researcher and study
  - Simple settings interface that allows the change of the initial notification time (or all notificaiton times 
    depending on the type of study), log out or even stop contributing to the study.
   -An introduction sequence on first start that shows a project logo, a small description, some project details and a consent page in which the consent is required to continue using the app.
   -As previously mentioned the application stores some information locally that allows for:
      > Questions and answers are stored locally to allow use even without an internet connection
      > Synchronization occurs when the application first launches during the authentication (if internet connection is established). If a connection to the internet is not available then the application uses the stored credentials and user information to launch.
      > Notifications times are set when the user changes or sets the notification time, and are set daily. The notifications are local.
    
