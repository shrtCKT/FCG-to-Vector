# FCG-to-Vector

Code for research paper "Scalable Function Call Graph-based Malware Classification" M. Hassen, P. K. Chan [https://dl.acm.org/citation.cfm?id=3029824](https://dl.acm.org/citation.cfm?id=3029824)

## Instructions

1. Run ExtractCallGraph to extract call graph representation from disassembled file. This will save the call graph representation in a .gml file.
2. Run CallGraphToFeatureVector to compute the vector representation of the input .gml file call graphs.