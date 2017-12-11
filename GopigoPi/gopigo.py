from picamera.array import PiRGBArray
from picamera import PiCamera
import threading
from bluetooth import *
from gopigo import *
import sys
import os
import subprocess
import atexit

import time
import cv2

import numpy as np

# chmod 777 /var/run/sdp
atexit.register(stop)

# ip : pi@192.168.137.163

def waitAndroid():
    socket_pi = BluetoothSocket( RFCOMM )
    socket_pi.bind(("", PORT_ANY))
    socket_pi.listen(1)

    port = socket_pi.getsockname()[1]
    uuid = "0000110a-0000-1000-8000-00805f9b34fb"

    advertise_service( socket_pi, "Gopigo", 
        service_id = uuid,
		service_classes = [uuid, SERIAL_PORT_CLASS],
        profiles = [SERIAL_PORT_PROFILE] )

    while True:
		print 'Waiting port {}'.format(port)
		socket_android, info_android = socket_pi.accept()
		print "Accepted connection from {}".format(info_android)
		return socket_android, socket_pi
        

class PiCameraConfig:

	def __init__(self, socket_android, socket_pi, resolution=(320, 160), framerate=32):
		self.camera = PiCamera()
		self.camera.resolution = resolution
		self.camera.framerate = framerate
		self.rawCapture = PiRGBArray(self.camera, size=resolution)
		self.stream = self.camera.capture_continuous(self.rawCapture, format="bgr", use_video_port=True)
		self.currentFrame = None
		self.stopStream = False
		self.socket_android = socket_android
		self.socket_pi = socket_pi
		self.canSendValue = False

	def startStream(self):
		threading.Thread(target=self.updateStream, args=()).start()
		return self

	def findRed(self, frame, dr, dg, db):
		width, height, channels = frame.shape

		for y in range(0, height):
			for x in range(0, width):
				b = frame.item(x, y, 0)
				g = frame.item(x, y, 1)
				r = frame.item(x, y, 2)

				if r > dr and g < db and b < db:
					frame.itemset((x, y, 0), 0)
					frame.itemset((x, y, 1), 0)
					frame.itemset((x, y, 2), 0)


	def updateStream(self):
		for frame in self.stream:
			self.currentFrame = frame.array
			img = cv2.medianBlur(self.currentFrame, 5)
			cimg = cv2.cvtColor(self.currentFrame, cv2.COLOR_BGR2GRAY)

			circles = cv2.HoughCircles(cimg, cv2.cv.CV_HOUGH_GRADIENT, 1, 20, param1=50, param2=60)
			stop()

			if circles is not None:
				circles = np.uint16(np.around(circles))

				count = 0

				for i in circles[0,:]:
					cv2.circle(cimg, (i[0], i[1]), i[2],(0, 255, 0), 2)
					cv2.circle(cimg, (i[0], i[1]), 2, (0, 0, 255), 3)

					if i[0] > 180:
						left()
					elif i[0] < 140:
						right()
					
					count = count+1
				
				if self.canSendValue == True:
					socket_android.send(str(count))
					print "{} transmis".format(str(count))
					self.canSendValue = False

			#		print( i[0] )
			#self.findRed(self.currentFrame, 120, 80, 80)
			cv2.imshow("Image", cimg)
			self.rawCapture.truncate(0)

			self.events()

			
	def events(self):
		key = cv2.waitKey(1) & 0xFF
		moveRobot(key)
				
		if key == ord("p"):
			self.stopStream = True

		if self.stopStream:
			self.stream.close()
			self.rawCapture.close()
			self.camera.close()
			self.socket_android.close()
			self.socket_pi.close()
			return


	def receivedData(self,socket_android):
		while True:
			data = socket_android.recv(1024)
			print "receivedData"
			if len(data) != 0:
				print "received {}".format(data)
				self.canSendValue = True

def moveRobot(key):
	if key == ord("q"):
		left_rot()
	elif key == ord("d"):
		right_rot()
	elif key == ord("z"):
		fwd()
	elif key == ord("s"):
		bwd()
	elif key == ord("a"):
		increase_speed()
	elif key == ord("e"):
		decrease_speed()
	elif key == ord("w"):
		stop()
	elif key == ord("x"):
		print "Exiting"
		sys.exit()		

	

canSendValue = False
socket_android, socket_pi = waitAndroid()
camera = PiCameraConfig(socket_pi, socket_android)
time.sleep(0.1)
threading.Thread(target=camera.receivedData, args=(socket_android,)).start()
camera.startStream()
