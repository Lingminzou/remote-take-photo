#-*- coding:utf-8 -*-
import os
import time
import BaseHTTPServer

count = 0x00

def CreateContent(path):

    path = "." + path

    content = ""

    try:
        with open(path, "r") as f:
            content += f.read()
    except IOError:
        print "文件不存在！！！"

    if("./index.html" == path):
        global count
        count += 1
        msg = "Welcome, {} visit, date: {}"
        msg = msg.format(count, time.ctime())
        content = content.replace("textMsg", msg)

    return content

class RequestHandler(BaseHTTPServer.BaseHTTPRequestHandler):

    def do_GET(self):
        self.send_response(200)
        if self.path.endswith(".html"):
            self.send_header("content-type", "text/html")
        elif self.path.endswith("favicon.ico"):
            self.send_header("content-type", "image/x-icon")
        else:
            self.send_header("content-type", "image/jpg")
        self.end_headers()
        self.wfile.write(CreateContent(self.path))

    def do_POST(self):

        print self.headers['user-agent']

        if("haha" == self.headers['user-agent']):

            length = int(self.headers['content-length'])
            data = self.rfile.read(length)

            with open("temp.jpg", "wb") as f:
                f.write(data)
                f.flush()

            os.remove("test.jpg")
            os.rename("temp.jpg", "test.jpg")

        self.send_response(200)
        self.end_headers()

if __name__ == '__main__':
    serverAddress = ('', 8080)
    server = BaseHTTPServer.HTTPServer(serverAddress, RequestHandler)
    server.serve_forever()

