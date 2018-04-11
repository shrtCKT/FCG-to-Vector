# FCG-to-Vector

Code for research paper "Scalable Function Call Graph-based Malware Classification" M. Hassen, P. K. Chan [https://dl.acm.org/citation.cfm?id=3029824](https://dl.acm.org/citation.cfm?id=3029824)

If you use this code please cite the paper.
```
@inproceedings{hassen2017scalable,
  title={Scalable Function Call Graph-based Malware Classification},
  author={Hassen, Mehadi and Chan, Philip K},
  booktitle={Proceedings of the Seventh ACM on Conference on Data and Application Security and Privacy},
  pages={239--248},
  year={2017},
  organization={ACM}
}
```

The code is provided "as is", without any guarantees.

## Instructions

1. Run ExtractCallGraph to extract call graph representation from disassembled file. This will save the call graph representation in a .gml file.
2. Run CallGraphToFeatureVector to compute the vector representation of the input .gml file call graphs.
