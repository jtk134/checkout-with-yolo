import requests

HOST = 'http://127.0.0.1:8000'
res = requests.post(HOST + '/api-token-auth/', {
'username':'jtk',
'password':'admin',
})

res.raise_for_status()
token = res.json()['token']
print(token)

# 인증이 필요한 요청에 아래의 headers를 붙임
headers = {'Authorization' : 'JWT ' + token, 'Accept' : 'application/json'}

# Post Create
data = {
'title' : '제목 by code',
'text' : 'API내용 by code',
'created_date' : '2022-06-07T18:34:00+09:00',
'published_date' : '2022-06-07T18:34:00+09:00'
}
file = {'image' : open('/Users/jtk/Downloads/son.jpg', 'rb')}
res = requests.post(HOST + '/api_root/Post/', data=data, files=file, headers=headers)
print(res)
print(res.json())