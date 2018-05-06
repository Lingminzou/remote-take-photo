#-*- coding:utf-8 -*-

import requests

headers = {'user-agent': 'haha'}

with open("./test.jpg", "r") as f:
    requests.post('http://127.0.0.1:8080', headers=headers, data=f)
	
