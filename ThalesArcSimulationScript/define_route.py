import json 
import requests
import osmapi as osm
import socket #api call
import numpy as np
import time
from tkinter import *
import math 

master = Tk()
master.geometry("500x200")

# Class function for the points
class Point:
    def __init__(self,long, lat):
        self.long = long
        self.lat = lat

api = osm.OsmApi() # this instantiate the OsmApi class,
# https://github.com/Project-OSRM/osrm-backend/blob/master/docs/http.md

# Connects to localhost socket at port 8001
def send_data(data): 
    HOST = '127.0.0.1'  # The server's hostname or IP address
    PORT = 8001        # The port used by the server
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect((HOST, PORT))
        s.sendall(data.encode('utf-8'))

# Return string from path
def return_path_string(point1, point2): 
    return f'{"{:.6f}".format(point1.lat)},{"{:.6f}".format(point1.long)};{"{:.6f}".format(point2.lat)},{"{:.6f}".format(point2.long)}'

def return_reponse(url):
    response = requests.get(url)
    text = json.loads(response.text)
    return text

# Return route nodes
def create_route_nodes(start, destination):
    # set1 = '-4.543259,48.393851;-4.4901305,48.413396'
    # set2 = '-4.478372,48.394309;-4.493333,48.394329;'
    route_path = return_path_string(start, destination)
    route = return_reponse(f'http://router.project-osrm.org/route/v1/driving/{route_path}?alternatives=false&annotations=nodes')
    nodes = route['routes'][0]['legs'][0]['annotation']['nodes']
    return nodes

def return_cords_nodes(nodes):
    points = []
    for node in nodes:
        new_node = api.NodeGet(node)
        points.append(Point(new_node['lon'], new_node['lat']))
    return points

# Returns distance for long / lat
def return_distance_cord(a, b):
    return a - b 

# Return the bearing from north to south
def return_bearing(point1, point2):
    distance_long = return_distance_cord(point2.long, point1.long )
    
    X = np.cos(point2.lat) * np.sin(distance_long)
    Y = np.cos(point1.lat) * np.sin(point2.lat) - np.sin(point1.lat) * np.cos(point2.lat) * np.cos(distance_long)
    bearing = np.arctan2(X,Y)
    return np.rad2deg(bearing)

# Returns the distance from point 1 to point 2 
def return_distance(point1, point2):
    R = 6373.0
    point1 = Point(np.radians(point1.long), np.radians(point1.lat))
    point2 = Point(np.radians(point2.long), np.radians(point2.lat))

    distance_long = return_distance_cord(point2.long, point1.long)
    distance_lat  = return_distance_cord(point2.lat, point1.long)

    a = np.sin(distance_lat / 2)**2 + np.cos(point1.lat) * np.cos(point2.lat) * np.sin(distance_long / 2)**2
    c = 2 * np.arctan2(np.sqrt(a), np.sqrt(1 - a))

    distance = R * c 

    return distance

# Returns a random cordinate from location x 
def return_random_cordinate(point, radius = 0.001):
    t = 2 * np.pi*np.random.uniform()
    R = (np.random.uniform(0,1) ** 0.5) * radius
    p = Point(point.long +R*np.cos(t), point.lat + R * np.sin(t))
    return p

# Returns sensor altitude, needs to be worked on
def return_altitude(sensor_altitude, point_altitude, distance):
    opp = abs(sensor_altitude - point_altitude)
    adj = distance 
    return math.degrees(math.atan(opp/adj))

# Main controller with hardcoded start and destination 
def controller():
    start, destination = Point(48.394360113263886, -4.493258325855184), Point(48.39823539179376, -4.4737747601544715)
    nodes = create_route_nodes(start, destination)
    cordinate_list = return_cords_nodes(nodes)
    id = 0
    # 10 for paths intervals
    for i in range(0, len(cordinate_list)):
        current_location = cordinate_list[i]
        future_location =  cordinate_list[i + 1]

        random_point = return_random_cordinate(current_location, 0.001)
        bearing = return_bearing(current_location, future_location)
        distance = return_distance(current_location, random_point)
        # random altitude, and target altitude
        sensor_altitude = np.random.randint(60, 80)
        target_altitude = np.random.randint(30, 110)
        sensor_elevation = return_altitude(sensor_altitude, sensor_altitude, distance)
        description = ""
        cord_json = {
            "id" : id,
            "sensor_latitude" : current_location.long, 
            "sensor_longitude" : current_location.lat, 
            "sensor_altitude" :  90,
            "sensor_azimuth" :  bearing,
            "sensor_elevation" : sensor_altitude,
            "target_latitude" :  random_point.long,
            "target_longitude" :  random_point.lat,
            "target_altitude" :  target_altitude,
            "target_range" : 100,
            "target_description" : description
        }    
        id += 1
        time.sleep(1)
        # Convert dict to json dump
        y = json.dumps(cord_json)
        send_data(y)

#set up button
button = Button(master,  command=lambda:controller(), text="Simulator" )
button.pack()  

# Display loop
master.mainloop()