#!/bin/bash
HOST=localhost
PORT=5432
USER=postgres

# you can use this environment variable to limit the amount of .csv files imported
if [[ -z "${LIMIT_CSV}" ]]; then
  CSV_AMOUNT=1000000
else
  CSV_AMOUNT="${LIMIT_CSV}"
fi

# start looping over rooms and their files
filecount=0
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
	
	# stop processing if you reached the desired amount of csv files
	((filecount++))
	if [[ "$filecount" -ge $CSV_AMOUNT ]]; then
       break
	fi
done
