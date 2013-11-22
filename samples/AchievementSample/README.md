![logo](http://update.orbotix.com/developer/sphero-small.png)

# Developer Guide: SpheroWorld and Achievements


## Overview
 
This guide will walk you through the basics of adding achievements to the SpheroWorld back end and adding their tracking to an iOS application.  

There is a sample code project for achievements in the dev center that has an example implementation of achievements and SpheroWorld that is a helpful companion to this guide.

### Create an application in SpheroWorld

Click on the “Dev” link in the top right corner of SpheroWorld after you are logged in to create a new application for your app.  The same application can be used for both Android and iOS applications.


![image01.png](https://github.com/orbotix/Sphero-iOS-SDK/raw/master/samples/AchievementSample/image01.png)

### Add achievements to your SpheroWorld application

Once your application has been created you can add achievements by clicking on the achievements tap when viewing the application details.  You can then click the “add achievement” button to add an achievement.

![yadda.png](https://github.com/orbotix/Sphero-iOS-SDK/raw/master/samples/AchievementSample/yadda.png)

After clicking **Add Achievement** you will see this screen:


![newache.png](https://github.com/orbotix/Sphero-iOS-SDK/raw/master/samples/AchievementSample/newache.png)



*All fields are required when adding an achievement:*

- **Achievement Name** - This is the short, human readable name displayed in achievements lists to identify the achievement.  (e.g. “Master Ball Handler” or “Speed Demon”)

- **Point Value** - This is the number of points the achievement is worth.  All of an app’s achievements can add up to no more than 1,000 points, but can be less.

- **Image** - This is the badge that is displayed in SpheroWorld to represent the achievement.

- **Description** - This is the description of the achievement displayed to the user. (e.g. “Drive Sphero 1 Mile” or “Collect 100 Widgets”).

- **Event Type** - This is a non-human readable string representing the event that happens to trigger the earning of this achievement.  This is never displayed by the user and only used inside your code to track achievement events. (e.g. an achievement that requires five holes in one for a golf game might use “holeInOne”)

- **Threshold** - This is an integer indicating how many events of this achievement’s type must happen before the achievement is earned.  This allows you to easily create progressive achievements for the same type of event while also allowing you to track more complex achievements in your own game code and make the threshold 1. (e.g. using the 5 hole-in-one achievement example from above you would put 5 as the threshold)

- **Notification When Won** - This is the message displayed to the user on SpheroWorld when the achievement has been earned.

## Integrating SpheroWorld into your Application

### Connect your application to SpheroWorld

The first step to integrate your application with SpheroWorld is to pass in your application id and secret string (*obtained from SpheroWorld as shown above*) when your main activity starts before you make any calls to RobotLibrary. 

![catball.png](https://github.com/orbotix/Sphero-iOS-SDK/raw/master/samples/AchievementSample/catball.png) 

Here is how to setup the achievement manager

    @Override
    protected void onStart() {
    	super.onStart();
		AchievementManager.setupApplication("sphe31bfdd87d4ef0877f9757772258f50ed", "sAPzGibRzGq25krvGzbi", this);
    }

In the main activity’s onStop method you also need to inform the achievement manager that the app is stopping so that it can gracefully close the database and free up resources. Call this in the `onStop()` method. 

    @Override
    protected void onStop() {
    	 super.onStop();
         AchievementManager.onStop();
    }

### Allowing app users to login to SpheroWorld

You can allow users to login to SpheroWorld and authorize your application by presenting the SpheroWorldWebView activity.  If a user hasn’t logged in and authorized your application they will be presented with a login screen.  If the user has already logged in they will be presented with a list of achievements and their progress towards earning them. Example code is shown below.

    Intent intent = new Intent(this, SpheroWorldWebView.class);
    startActivity(intent);

### Add achievement event tracking to your application

You will need to add code throughout your app to track achievement related events.  A few examples are shown below.

    public void redPressed() {
		AchievementManager.recordEvent(“red”);
    }

This example shows how to record an event with a count of 1.

    public void gotScore(int score) {
		AchievementManager.recordEvent(“totalPoints”, score);
    }

This example shows how to record an event by passing in a dynamic count other than 1.  This particular example could be used to give the user achievements for total points earned over all time.

    public void gameOver() {
	    if(mySuperSpecialCondition)  {
            AchievementManager.recordEvent(“capturedAllTheBasesAndKilledAllTheDudes”);
        }
    }

This example shows how achievements with complex threshold conditions can be tracked internally within your game and used with SpheroWorld achievements with a threshold of 1.

### Notifying the user of earned achievements

You can listen for the AchievementManager.AchievementEarnedIntent so you can notify the player when the user has earned an achievement.  A JSON string containing the achievement meta data can be found as a string extra attached to the intent under the key “achievementJSON”.  Currently RobotLibrary on Android doesn’t provide a UI to notify users they earned an achievement. But you definitely can! 

## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)