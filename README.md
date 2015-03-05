#Judgels Jophiel

##Description
Jophiel is an application built using [Play Framework](https://www.playframework.com/) to provide single sign on functions and services.

Jophiel provide single sign on based on [Open Id Connect](http://openid.net/connect/) protocol.

##Set Up And Run
To set up Jophiel, you need to:

1. Clone [Judgels Play Commons](https://github.com/ia-toki/judgels-play-commons) into the same level of Jophiel directory, so that the directory looks like:
    - Parent Directory
        - judgels-play-commons
        - judgels-jophiel

2. Copy conf/application_default.conf into conf/application.conf and change the configuration accordingly. **Refer to the default configuration file for explanation of the configuration keys.** 

3. Copy conf/db_default.conf into conf/db.conf and change the configuration accordingly. **Refer to the default configuration file for explanation of the configuration keys.** 

To run Jophiel, just run "activator" then it will check and download all dependencies and enter Play Console.
In Play Console use "run" command to run Jophiel. By default it will listen on port 9000. For more information of Play Console, please read the [documentation](https://www.playframework.com/documentation/2.3.x/PlayConsole).

After login on Jophiel, add "admin" value separated by "," to role column of your user record on table "jophiel_user" then relogin (logout and login again) to access full feature.

The version that is recommended for public use is [v0.1.0](https://github.com/ia-toki/judgels-jophiel/tree/v0.1.0). The online version can be seen on [here](http://sso.ia-toki.org). **Note: The online version is not always available at the moment.**
