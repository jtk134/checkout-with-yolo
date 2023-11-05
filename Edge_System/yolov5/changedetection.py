import os
import cv2
import pathlib
import requests
from datetime import datetime

objects_dict = {
    "backpack": 20000, 
    "umbrella": 5000,
    "handbag": 30000, 
    "tie": 10000, 
    "suitcase": 50000, 
    "frisbee": 5000, 
    "sports ball": 6000, 
    "kite": 5000, 
    "baseball bat": 20000, 
    "baseball glove": 50000, 
    "tennis racket": 60000, 
    "bottle": 4000,
    "wine glass": 5500, 
    "cup": 3000, 
    "fork": 3000, 
    "knife": 3000, 
    "spoon": 2000, 
    "bowl": 5000, 
    "banana": 3000, 
    "apple": 1000, 
    "sandwich": 4000, 
    "orange": 1500, 
    "broccoli": 1000, 
    "carrot": 1000, 
    "hot dog": 3000, 
    "pizza": 15000, 
    "donut": 2500, 
    "cake": 25000, 
    "laptop": 300000, 
    "mouse": 20000, 
    "remote": 10000, 
    "keyboard": 30000, 
    "cell phone": 500000, 
    "book": 10000, 
    "clock": 9000, 
    "vase": 9000, 
    "scissors": 1500, 
    "teddy bear": 10000, 
    "hair dryer": 30000, 
    "toothbrush": 2500
}



class ChangeDetection:
    result_prev = []
    HOST = 'https://jtk.pythonanywhere.com' #'http://127.0.0.1:8000'
    username = 'jtk'
    password = 'admin'
    token = ''
    title = ''
    text = ''

    def __init__(self, names):
        self.result_prev = [0 for i in range(len(names))]
        
        res = requests.post(self.HOST + '/api-token-auth/', {
            'username': self.username,
            'password': self.password,
        })
        res.raise_for_status()
        self.token = res.json()['token'] #토큰 저장
        print(self.token)

    def add(self, names, detected_current, save_dir, image):
        self.title = ''
        self.text = ''
        change_flag = 0 #변화 감지 플레그
        i = 0
        while i < len(self.result_prev):
            if self.result_prev[i]==0 and detected_current[i]==1 :
                if names[i] in objects_dict:
                    change_flag = 1
                    self.title = names[i]
                    self.text += names[i] + " : " + str(objects_dict[names[i]]) + " won, "
            i += 1

        self.result_prev = detected_current[:] # 객체 검출 상태 저장

        if change_flag==1:
            self.send(save_dir, image)

    def send(self, save_dir, image):
        now = datetime.now()
        now.isoformat()

        today = datetime.now()
        save_path = os.getcwd() / save_dir / 'detected' / str(today.year) / str(today.month) / str(today.day)
        pathlib.Path(save_path).mkdir(parents=True, exist_ok=True)

        full_path = save_path / '{0}-{1}-{2}-{3}.jpg'.format(today.hour,today.minute,today.second,today.microsecond)
        
        dst = cv2.resize(image, dsize=(320, 240), interpolation=cv2.INTER_AREA)
        cv2.imwrite(full_path, dst)

        # 인증이 필요한 요청에 아래의 headers를 붙임
        headers = {'Authorization' : 'JWT ' + self.token, 'Accept' : 'application/json'}

        # Post Create
        data = {
        'title' : self.title,
        'text' : self.text,
        'created_date' : now,
        'published_date' : now
        }
        file = {'image' : open(full_path, 'rb')}
        res = requests.post(self.HOST + '/api_root/Post/', data=data, files=file, headers=headers)
        print(res)
