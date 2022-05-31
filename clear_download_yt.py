import os
import sys
from yt_dlp import YoutubeDL
from bs4 import BeautifulSoup


def download_video(soup):
    def reaplce_path(path: str) -> str:
        path = path.replace('#', '%23')
        return path


    def find_files(id_yt, video_files):
        for video_file in video_files:
            if id_yt in video_file:
                return video_file
        return ''

    os.makedirs('video', exist_ok=True)
    os.makedirs('poster', exist_ok=True)
    # vstavka = """\n<video controls="" poster="poster/{}" style="height: 100%; width: 100%; object-fit: contain">
# <source src="video/{}"></video>"""
    vstavka_url = {
        'vk': "https://vk.com/video{}",
        'vimeo': "https://vimeo.com/{}",
        'youtube': "https://youtu.be/{}"
    }
    ydl_opts = {
        'ignoreerrors': True,
        'writethumbnail': True,
        'concurrent_fragment_downloads': 8,
        'outtmpl': {
            'default': './video/%(title)s [%(id)s].%(ext)s',
            'thumbnail': './poster/%(title)s [%(id)s].%(ext)s'
        }
    }
    tg = soup.find_all(attrs={'class': 'andropov_video'})
    for i in range(len(tg)):
        try:
            video_id = tg[i]['data-video-service-id']
        except KeyError:
            continue
        url_download = vstavka_url[tg[i]['data-video-service']].format(video_id)
        with YoutubeDL(ydl_opts) as dl:
            dl.download([url_download])

        poster_files = os.listdir('poster')
        video_files = os.listdir('video')

        poster = reaplce_path(find_files(video_id, poster_files))
        video = reaplce_path(find_files(video_id, video_files))

        if video != '':
            # tg[i].replace_with(vstavka.format(poster, video))
            video_vst = soup.new_tag(
                "video",
                attrs={
                    'poster': f"poster/{poster}",
                    'style': "height: 100%; width: 100%; object-fit: contain",
                    'controls': ''
                }
            )
            source = soup.new_tag(
                "source",
                attrs={'src': f"video/{video}"}
            )
            video_vst.append(source)
            tg[i].replace_with(video_vst)
    return soup


def delete_class(soup, class_name='andropov_video--service-youtube'):
    tg = soup.find_all(attrs={"class": class_name})
    for i in range(len(tg)):
        tg[i].replace_with("")
    tg = soup.find_all(attrs={"class": class_name})
    return soup


def delete_editorial(soup):
    tg = soup.find_all("a", href="/editorial")
    for i in range(len(tg)):
        tg[i].replace_with("")
    return soup


if __name__ == "__main__":
    os.chdir(sys.argv[1])
    with open('index.html', encoding='utf-8') as f:
        data = f.read()
    data = BeautifulSoup(data, features="html.parser")

    data = download_video(data)
    data = delete_class(data)
    data = delete_class(data, "propaganda")
    data = delete_class(data, "icon--entry_quote")
    data = delete_class(data, "content-header__item--listen")
    data = delete_editorial(data)

    with open('index.html', 'w', encoding='utf-8') as f:
        f.write(str(data))
