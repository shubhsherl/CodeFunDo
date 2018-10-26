import pickle
file = open("diff.pkl",'rb')
clf=pickle.load(file)
from sklearn.feature_extraction.text import HashingVectorizer
text=[]
text.append("Birmingham Wholesale Market is ablaze BBC News - Fire breaks out at Birmingham's Wholesale Market http://t.co/irWqCEZWEU")
import numpy as np
import nltk
from nltk import pos_tag
from nltk.tokenize import word_tokenize
from nltk.chunk import conlltags2tree
from nltk.tree import Tree
import SearchAPI
import json
from pprint import pprint
from statistics import stdev
import sys

def process_text(txt_file):
    raw_text = txt_file
    token_text = word_tokenize(raw_text)
    return token_text

def nltk_tagger(token_text):
    tagged_words = nltk.pos_tag(token_text)
    clean_tags = []
    for (i,j) in tagged_words:
        if(j=='NN'):
            clean_tags.append((i,j))
    ne_tagged = nltk.ne_chunk(tagged_words)
    return(ne_tagged)

def structure_ne(ne_tree):
    ne = []
    for subtree in ne_tree:
        if type(subtree) == Tree: # If subtree is a noun chunk, i.e. NE != "O"
            ne_label = subtree.label()
            ne_string = " ".join([token for token, pos in subtree.leaves()])
            ne.append((ne_string, ne_label))
    return ne

def nltk_main(txt):
    return (structure_ne(nltk_tagger(process_text(txt))))

def get_tags(txt):
    ner_tags = nltk_main(txt)
    person = []
    loc = []
    for (i,j) in ner_tags:
        if(j=='PERSON'):
            person.append(i)
        elif(j=='LOCATION' or j=='GPE'):
            loc.append(i)
    return (person,loc)

def clean_up(arr,c=20):
    if((len(arr)-len(set(arr)))/len(arr)>=0.8):
        return set(arr)
    else:
        freq = nltk.FreqDist(arr)
        arr_im = []
        if(c==0):
            for i in freq:
                if(freq[i]<=1):
                    if(len(i)>=8 and len(i)<=18):
                        arr_im.append(i)
            return set(arr_im)
        threshold = c * len(set(arr))/len(arr)
        for i in freq:
            if(freq[i]>=threshold):
                arr_im.append(i)
        return set(arr_im)

def run(txt):
    (p,l) = get_tags(txt)
    p = clean_up(p,c=0)
    l = clean_up(l,c=5)
    return (list(p), list(l))


def relevant(tweet):
    text.append(tweet['text'])
    vectorizer = HashingVectorizer(stop_words='english', alternate_sign=False,n_features=2**16)
    x=vectorizer.transform(text)
    r=clf.predict(x)
    if r==0:
        return 0
    else:
        return 1


def get_info(disaster):
    tweets=SearchAPI.fetch(disaster['name'],geocode=disaster['location'],count=10)
    if len(tweets)==0:
        disaster['got_tweets']=False
        return disaster
    disaster['got_tweets']=True
    for tweet in tweets:
        r=relevant(tweet,disaster)
        lat=[]
        longi=[]
        if(r==1):
            disaster['people'],disaster['places']=run(tweet['text'])
            lat.append(tweet['coordinate'][0])
            longi.append(tweet['coordinate'][1])
        disaster['tweet_center']=[sum(lat)/float(len(lat)),sum(longi)/float(len(longi))]
        disaster['rad']=[stdev(lat),stdev(longi)]
        return disaster
file_name=sys.argv[1]
file=open(file_name)
disasters=json.load(file)
final=[]
for dis in disasters:
    disaster={}
    disaster['name']=dis['dc_subject'][0]
    disaster['location']=str(dis['foaf_based_near'][0])+','+str(dis['foaf_based_near'][1])+','+'10km'
    pprint(disaster)
    disast=get_info(disaster)
    dis['got_tweets']=disast['got_tweets']
    if disast['got_tweets']==True:
        dis['people']=disast['people']
        dis['location']=disast['location']
        dis['tweet_center']=disast['tweet_center']
        dis['rad']=disast['rad']
    final.append(dis)
pprint(final)
fl=open('with_tweets.json','w')
json.dump(final,fl)

# tweetCriteria = GetOldTweets.got.manager.TweetCriteria().setQuerySearch('cyclone').setSince("2018-10-10").setUntil("2018-10-15").setMaxTweets(100).setNear('21.7,85.6').setWithin('10mi')
# tweet = GetOldTweets.got.manager.TweetManager.getTweets(tweetCriteria)[0]
# print(tweet.text)
