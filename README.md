# 一、改变旧手机的命运

就在前几天我终于。。。换新手机啦，哈哈~

看着用了两年的旧手机其实也是可以正常使用的，就这样丢抽屉吃灰了吗？

不不不，生命不息，折腾不止。。。

可以用它来做类似监控摄像头的东西啊！

对，那就先来做一个自动拍照并上传至 web 服务器的功能吧，

这样我就可以通过浏览器远程来查看它拍摄的照片，也有点远程监控的意思对不，

还记得我有一个远在美帝的服务器了吗，正好耍耍 ^_^

先来看看最后完成的效果吧。

[点我](http://23.95.214.128:8080/index.html) 或 复制下面的链接到浏览器打开即可查看现在服务器上保存的图片。

http://23.95.214.128:8080/index.html

特别的因为服务器在美帝的原因，访问速度比较慢。

我拍照的姿势大概是这样的，它每过 5 分钟即会拍摄一张图片上传到服务器。

![](http://wx3.sinaimg.cn/mw690/9e169b75gy1fr1radqi6dj20u0140jti.jpg)

为了证明它是会更新的，我还特意摆上了我的温湿度计，上面有时间会更新。

但我可能也就是今天玩玩，后面就把它撤了，或者换成别的姿势，不知道你们想看什么，哈哈~

（接下来的内容全是我现学现用，写的不到位也还请谅解）

# 二、搭建简易 web 服务器

我对这个 web 服务器的需求很简单，我需要它：

1. 能提供一个简易的网页，上面显示一张图片
2. 能接收上传图片

也就是支持 http 的 GET 和 POST 请求就行了啊

那就用 python 来做吧。人生苦短，我要用 python。

这里我用了 python 的 BaseHTTPServer 模块。

关于它的介绍请参看下面这篇文章吧，我写的肯定没他好 ^_^

http://cizixs.com/2016/05/20/python-httpserver

更官方的介绍请看这里：

https://docs.python.org/2/library/basehttpserver.html

那来看下我写的吧，

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

在 do_GET 函数中我们响应浏览器的请求，self.path 中表明了浏览器想要的是那个文件，

在回复给浏览器的时候我们要填充一个 content-type 的内容，特殊的我这里只处理了 html 和 favicon.ico，

然后其他的所有请求我都认为它是在要图片。。。简单粗暴吧，然后具体的数据通过函数 CreateContent 来构建，我们后面再看。

在 do_POST 函数中我们响应一个 POST 请求，即上传数据，这里我们只接收一样东西，为了拒绝其他上传的东西，

我利用 user-agent 设置了一个暗号，为 haha 的才是我上传的图片，实际它不是这样的用的哈。。。

收到图片后就保存起来并替换掉用于网页显示的 test.jpg，总体过程就是。

那我们再来看下 CreateContent 的内容，如下：

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

应该很容易理解吧，就是要什么读什么给它。

特殊的，针对 index.html 我为了要显示一个访问计数和时间，在读出来后加工了一下。

到这里我需要的两点需求就达成了，一点小开心

# 三、来拍个照吧

Android app 好久没写了，我写 app 的时候 android 最新版本还是 4.4，现在都 8.1+ 了。

不过没关系，先找个示例吧，如下，我找到一个控制摄像头拍摄一张图片的例子

https://github.com/googlesamples/android-Camera2Basic

那我就在它上面加上自动拍摄然后上传的功能就好了啊。

代码我们就不看了，我直接上 github 路径吧，如下：

https://github.com/Lingminzou/remote-take-photo

我主要做了下面几件事：

1. 增加了一个倒计时，计时到了后调用拍照接口进行拍照
2. 由于拍摄的原始图片较大，所以上传前做了压缩
3. 然后就是 http 的 POST 上传压缩过后的图片
4. 由于2、3两件事比较耗时所以开了线程来做它们

# 四、最后总结一下

折腾了一个周末总算也是达成目标了，可是只得到一小点开心，因为我做的还不是特别好

有空的话我要把它折腾成实时的视频监控，不过那得等我换个服务器才行。。。

最后再说一个关于服务器上遇到的问题吧，我是用 ssh 连接我的服务器的，把代码传上去后，

我自信的敲下了 `python server.py &` 让我的 web 服务器后台运行，

然后我断开 ssh 后发现访问不了了，再上去看发现它不在了。。。

后面了解到原来这种情况下需要使用 nohup 命令才管用，下面这样

	nohup python server.py &

好了，今天到这里吧，我要休息下了，周末愉快！

扫码关注我了解更多

![](http://wx1.sinaimg.cn/large/9e169b75gy1fqcisgsbd7j2076076q3e.jpg)