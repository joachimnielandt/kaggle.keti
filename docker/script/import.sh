#!/bin/bash
HOST=localhost
PORT=5432
USER=postgres

for d in /docker-entrypoint-initdb.d/data/*/ ; do
    ROOM=`basename "$d"`
    echo "$d"
    echo "room: $ROOM"
    # create the room
    psql -c "insert into room (name) values ('$ROOM');" -U postgres
	
	# import the raw files
	for type in co2 pir temperature light humidity; do
		# import the $type file
		psql -c "copy $type(timestamp, measurement) from '/docker-entrypoint-initdb.d/data/$ROOM/$type.csv' delimiter ',' csv;" -U postgres
		# set the room in $type
		psql -c "update $type set idroom = (select idroom from room where name = '$ROOM') where idroom is null" -U postgres
	done
done