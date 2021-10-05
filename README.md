# Introduction

This package contains two modules: database and backend. Both are controlled and set up by Docker compose. 

# Getting started

Run the following commands in the main directory (which contains `docker-compose.yml`).

- `docker-compose build`
- `docker-compose up -d`

The first one builds the `backend` java project which hosts the HTTP SSE endpoint. The second command then brings up the database and backend.

# Database

By default, you can access the database on `localhost:5432` through your favorite database application, e.g., Datagrip. Creating the database can take a while: it has to import all the KETI .csv files (500+MB in total). It takes a while to load the large .csv files, so you can't connect to the database straight away. Check up on progress by running `docker logs kaggle-keti -f`. Once the database is up, connect to it.

Use, e.g., Datagrip to connect to the database. By default, the database runs on `localhost:5432`. Customize this port in the `.env` file if you wish. Use the following default credentials, or your own if you customised the setup:

- host: localhost
- port: 5432
- user: postgres
- password: <see `docker-compose.yml`>

The actual data files were taken from Kaggle.com (https://www.kaggle.com/ranakrc/smart-building-system). The dataset should be unpacked in the `./database/data` folder. So, for example, the full path of one of the .csv files is:

`.\database\data\413\co2.csv`

Only when the files are in the correct location can the docker process successfully build the database.

# Backend

You can access the backend yourself through (default) port 7000. Surf to `localhost:7000` to see the Hello world landing page. Perform an HTTP request to `localhost:7000/sse` to get the SSE stream of KETI events. To get a stream of events in a program working it is usually easiest to do this programmatically. The `backend` module contains an example 'Client' application. Open `backend` in Intellij IDEA and run the main function in `Client.java`.

Je kan de backend steeds herstarten (moest er iets mis gaan) door dit commando uit te voeren (de naam van de container kan veranderen afhankelijk van je gekozen directory):

`docker restart kaggleketi_backend_1`

Je kan de logs er ook van bekijken (dit bevat eventuele foutmeldingen en output die verstuurd wordt naar de gebruikers):

`docker logs kaggleketi_backend_1`

# .env file

Make a copy of `.env.example` and name it `.env`. Override any defaults to your own liking in the file and use the docker compose file. The `.env` file is ignored in the git repository, so it stays local to your own environment. If no `.env` file is present, the defaults defined in `docker-compose.yml` will be used instead.

A quick overview of the options:

- DB_PORT: the port on which the database is exposed on your local machine. Needed if you want to connect to it yourself.
- LIMIT_CSV: if you want to limit the amount of CSV files that are loaded by the database, you can do that here