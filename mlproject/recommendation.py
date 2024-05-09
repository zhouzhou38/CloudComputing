from surprise import Dataset, Reader
from surprise.model_selection import train_test_split
from surprise.prediction_algorithms.matrix_factorization import SVD
from surprise import accuracy
import pandas as pd
import mysql.connector
import threading 
import socket
import re
import json
import pgeocode

# Replace the following placeholders with your connection details
config = {
    'user': 'root',
    'password': 'pomegrante2000$$JUICES',
    'host': '35.192.120.50',
    'database': 'P2P',
    'port': 3306  # optional, default is 3306
}

try:
    conn = mysql.connector.connect(**config)
    cursor = conn.cursor()
    print("Connection successful!")
except mysql.connector.Error as e:
    print(f"Error connecting to MySQL Platform: {e}")

cursor.execute("SELECT * FROM owners_table")
rows = cursor.fetchall()
owner_data_table = rows

cursor.execute("SELECT * FROM car_data_table")
# Fetch all rows from the executed query and print them
rows = cursor.fetchall()
car_data_table = rows
# Ml Recommendation system
cursor.execute("SELECT * FROM car_ownership_table")
# Fetch all rows from the executed query and print them
rows = cursor.fetchall()
dataset = rows

data = [(userID, carID, 1) for userID, carID, _, _, _ in dataset]
reader = Reader(line_format='user item rating', rating_scale=(0, 1))
dataset = Dataset.load_from_df(pd.DataFrame(data, columns=['userID', 'carID', 'rating']), reader)

trainset, testset = train_test_split(dataset, test_size=0.2)

model = SVD()
model.fit(trainset)
predictions = model.test(testset)
#print("RMSE:", accuracy.rmse(predictions))

def get_top_n_recommendations(model, user_id, n=10):
    # Get a list of all car IDs
    all_car_ids = dataset.df['carID'].unique()

    # Predict ratings for all car IDs that the user hasn't interacted with
    unrated_car_ids = []
    for car_id in all_car_ids:
        prediction = model.predict(user_id, car_id)
        if not prediction.details['was_impossible']:
            unrated_car_ids.append((car_id, prediction.est))

    # Sort the unrated car IDs based on predicted ratings and return the top n
    top_n = sorted(unrated_car_ids, key=lambda x: x[1], reverse=True)[:n]
    return top_n

user_id = 123
recommendations = get_top_n_recommendations(model, user_id)
#print("Top 10 recommendations for user", user_id, ":", recommendations)



def get_popular_recommendations(dataset):
    # Aggregate ratings across all users and sort by popularity or average rating
    popular_items = dataset.df.groupby('carID')['rating'].mean().sort_values(ascending=False)
    return popular_items.index.tolist()

recommendations = get_popular_recommendations(dataset)
print("Popular recommendations for new user:", recommendations)
firstcarinformation = car_data_table[recommendations[0]-1]
print(firstcarinformation)

def convertTuple(tup):
    currentstr = ''
    for x in tup:
        currentstr += str(x) + ' '
    return currentstr


ip_pattern = r"\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}"

def getdatasetdata(myjson,myquery):
    params = []
    for key, value in myjson.items():
        if key == 'msrp' or key=='asking_price' or key=='engine_displacement_size' or key == 'num_doors' or key=='num_cylinders' or key=='horsepower' or key=='num_seat_rows' or key=='num_seats' or key =='is_turbocharged':
            if value.startswith('<'):        
                myquery += f"{key} < %s AND "
                params.append(value[1:])
            else:
                myquery += f"{key} > %s AND "
                params.append(value[1:])
        else:
            myquery += f"{key} = %s AND "
            params.append(value)

    myquery = myquery[:-5] + ";"
    print(myquery)
    return myquery,params

def listen_on_port(ip, port):
    # Create a socket for listening
    listen_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    send_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    # Bind the socket to the IP and port
    listen_socket.bind((ip, port))
    print(f"Listening on {ip}:{port}")

    try:
        while True:  # Keeps listening until manually stopped
            # Receive data (you can adjust the buffer size)
            redata, addr = listen_socket.recvfrom(1024)
            redata = redata.decode()
            print(f"Received message: {redata} from {addr}")
            myquery = "SELECT * FROM car_data_table WHERE " 
            zipcodeflag = False
            myjson = json.loads(redata)
            try:
                zipcode = myjson["zip_code"]
                del myjson["zip_code"]
            except:
                zipcodeflag = True
            
            ipaddress = myjson["IPaddress"]
            del myjson["IPaddress"]
            
            myquery,params = getdatasetdata(myjson,myquery)
            cursor.execute(myquery, params)
            results = cursor.fetchall()
            
            if len(results) == 1:
                fircarinformation = car_data_table[results[0][-1]-1]
                print(fircarinformation)
            else:
                fliterindex = [eachtuple[-1] for eachtuple in results]
                bestidx = float('inf')
                for i in fliterindex:
                    #print(recommendations)
                    if i == 252 or i == 253:
                        continue
                    currentidx = recommendations.index(i)
                    if currentidx < bestidx:
                        bestidx = currentidx
            
                fircarinformation = car_data_table[recommendations[bestidx]-1]
                print("first car information: ", fircarinformation)
            currentcarinformation = convertTuple(fircarinformation)
            print("currentcarinformation ", currentcarinformation)
            if not zipcodeflag:
                carID = fircarinformation[-1]
                usrlist = []
                
                print("*" * 100)
                for user in data:
                    if user[1] == carID:
                        usrlist.append(user)
        

                newuserlist = []
                dist = pgeocode.GeoDistance('US')
                currentdistance = float('inf')
                for eachuser in usrlist:
                   ownserzipcode = owner_data_table[eachuser[0]-1][-2]

                   distance = dist.query_postal_code(int(zipcode), int(ownserzipcode))
                   if distance != 'nan':
                       newuserlist.append(eachuser + (distance,))

                newuserlist = sorted(newuserlist,key=lambda x:x[-1])
                nearestowner = owner_data_table[newuserlist[0][0]-1]
                print(nearestowner)
                currentnearestowner = convertTuple(nearestowner[:-1])
                sentinformation = 'Current nearest owner: ' + currentnearestowner + '\n' + "Recommendation car: " + currentcarinformation
                send_socket.sendto(sentinformation.encode(), (ipaddress, port))
                print(f"Sent to successful!")
            else:
                sentinformation = "Recommendation car: " + currentcarinformation
                send_socket.sendto(currentcarinformation.encode(), (ipaddress, port))
                print(f"Sent to successful!")
    finally:
        # Close the listening socket when done
        listen_socket.close()
        print("Socket closed")


listen_ip = '0.0.0.0'
listen_port = 8888

thread = threading.Thread(target=listen_on_port, args=(listen_ip, listen_port))
thread.start()





