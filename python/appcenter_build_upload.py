import sys
import requests
import glob
import os
import json
import re

if len(sys.argv) < 4:
    print 'Missing input parameters', len(sys.argv)
    exit()

TOKEN = sys.argv[1]
USERNAME = sys.argv[2]
PROJECT_NAME = sys.argv[3]
TAG = ''
if len(sys.argv) >= 5:
    TAG = sys.argv[4]

print 'params:', sys.argv[1:]

def human_size(bytes, units=[' b','KB','MB','GB','TB', 'PB', 'EB']):
    return str(bytes) + units[0] if bytes < 1024 else human_size(bytes>>10, units[1:])

headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'X-API-Token': TOKEN
}

def prepare_upload():
    url = "https://api.appcenter.ms/v0.1/apps/%s/%s/release_uploads" % (USERNAME, PROJECT_NAME)
    return requests.post(url = url, headers=headers).json()

def upload_apk(upload_url):
    try:
        apk_file = glob.glob('app/build/outputs/*/*/*/*.apk')[0]
    except:
        apk_file = glob.glob('app/build/outputs/*/*/*.apk')[0]
    apk_ptr = open(apk_file, 'rb')
    print 'Start uploading', apk_file, human_size(os.stat(apk_file).st_size)
    files = {'ipa': (os.path.basename(apk_file), apk_ptr, 'application/vnd.android.package-archive', {'Expires': '0'})}
    print 'Uploading to', upload_url
    requests.post(url = upload_url, files=files)
    apk_ptr.close()
    print 'Done uploading build'

def patch_upload_status(upload_id):
    patch_url = 'https://api.appcenter.ms/v0.1/apps/%s/%s/release_uploads/%s' % (USERNAME, PROJECT_NAME, upload_id)
    print patch_url
    patch_data = json.dumps({'status':'committed'})
    return requests.patch(url = patch_url, headers=headers, data = patch_data).json()

def set_upload_group(release_url, destinations):
    patch_url = 'https://api.appcenter.ms/%s' % release_url
    patch_data = json.dumps(
        {
            'destination_name':destinations,
            'release_notes':''
         }
    )
    requests.patch(url = patch_url, headers=headers, data = patch_data)

def set_upload_all_group(release_url, distribution_groups):
    patch_url = 'https://api.appcenter.ms/%s' % release_url
    patch_data = json.dumps(
        {
            'destinations':distribution_groups,
            'release_notes':''
        }
    )
    requests.patch(url = patch_url, headers=headers, data = patch_data)

def get_distribution_groups():
    get_url = 'https://api.appcenter.ms/v0.1/apps/%s/%s/distribution_groups' % (USERNAME, PROJECT_NAME)
    return requests.get(url = get_url, headers=headers).json()



data = prepare_upload()
upload_apk(data['upload_url'])
status_data = patch_upload_status(data['upload_id'])

destre = re.search('[act]?$', TAG, flags=re.IGNORECASE)
if destre:
    dest = destre.group(0).upper()
else:
    dest = None

print 'DESTINATIONS: %s' % dest

if not dest:
    set_upload_group(status_data['release_url'], "Testers")
elif dest == 'A':
    set_upload_all_group(status_data['release_url'], get_distribution_groups())
elif dest == 'C':
    set_upload_group(status_data['release_url'], "Collaborators")
elif dest == 'T':
    set_upload_group(status_data['release_url'], "Testers")

print 'All done'