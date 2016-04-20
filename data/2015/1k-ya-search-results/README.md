Search Results from Yahoo! Answer site
=========
This dataset contains search results that were collected from [Yahoo! Answer] site. 


Description
--------------
LiveQA team provided [a sample](https://github.com/yuvalpinter/LiveQAServerDemo/blob/master/data/1k-qids.txt) of 1000 Yahoo! Answers question IDs (QIDs).
We used each question's text as input query, and then collected outputs from [Yahoo! Answer] site's default ''Search Answer'' function.
Specifically, a list of returned QIDs was collected by its "Relevance" search (instead of "Newest", "Most Answers", or "Fewest Answers"). Only the first 100 results of each query are saved.
Both queries and search results in this dataset are in form of QIDs. 
The texts of question and answers can be extracted online using the example code provided by LiveQA organizers. 

This dataset may be used by researchers to learn and validate answer
re-ranking models for web question answering.


Folder Contents
--------------

* `1k-qids.results`: [Yahoo! Answer] search results file in TREC result submission format,

* `1k-qids.qrel`: Automatically generated TREC qrels file that labels only input query's QID as relevant document.
 
* `1k-qids.treceval`: Example `trec_eval` output on this dataset.

* `1k-qids.error`: Four QIDs whose question text could not be extracted correctly, and excluded in this dataset.

Evaluation
--------------

To evaluate search results with trec_eval:
```sh
trec_eval 1k-qids.qrel 1k-qids.results -c > 1k-qids-ya.treceval
```

Version
----
1.0

License
----
Apache License, Version 2.0

Authors
----
- Di Wang  (diwang@cs.cmu.edu)
- Eric Nyberg (ehn@cs.cmu.edu)

[Yahoo! Answer]:https://answers.yahoo.com/
