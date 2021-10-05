# .env file

Make a copy of `.env.example` and name it `.env`. Override any defaults to your own liking in the file and use the docker compose file. The `.env` file is ignored in the git repository, so it stays local to your own environment. If no `.env` file is present, the defaults defined in `docker-compose.yml` will be used instead.

# start the database

Run `docker-compose up -d` to bring the database up. It takes a while to load the large .csv files, so you can't connect to the database straight away. Check up on progress by running `docker logs kaggle-keti -f`. Once the database is up, connect to it.

# connect to the database

Use, e.g., Datagrip to connect to the database. By default, the database runs on `localhost:5432`. Customize this port in the `.env` file if you wish. Use the following default credentials, or your own if you customised the setup:

- host: localhost
- port: 5432
- user: postgres
- password: <see `docker-compose.yml`>

