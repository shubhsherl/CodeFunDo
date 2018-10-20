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


def processor(tweet):
    text.append(tweet['text'])
    vectorizer = HashingVectorizer(stop_words='english', alternate_sign=False,n_features=2**16)
    x=vectorizer.transform(text)
    disaster=clf.predict(x)
    if disaster=0:
        return 0
    else:
        run(tweet(txt))

