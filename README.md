# SOA RAG Reference Architecture

In this post we’re going to explore a SOA RAG reference architecture.

<figure>
<img src="./assets/images/ReferenceArch.png" alt="ReferenceArch" />
</figure>

# High Level

One key to efficient Retrieval Augmented Generation is to pre-process
segment data and query embeddings before a user required to use them.

To achieve this an architect may design a data pipeline that contains
several stages.

## Data Source

Data sources from our point of view are anything that produces data that
our system consumes.

In our demo architecture we use JavaFaker to produce inputs to our
pipeline.

## Data Landing Zone

When data is created and transmitted to our system, the collection of
endpoints, brokers, and other services which collect primary data live
here.

Examples: Apache ActiveMQ, Apache Kafka

In our demo architecture we use Apache ActiveMQ as a JMS data sink.

## ETL

Extract, Transform, Load is a design paradigm.

Incoming data is first extracted, then transformed into something we can
process, the data may be enriched, then sent (loaded) for further
processing or storage. In modern implementations business rules for
cleaning/filtering data may be augmented with Machine Learning to
improve data quality & apply metadata for improved reuse.

Examples: Apache Camel, Apache Flink

In our demo architecture we use Apache Camel to implement an ETL
pattern.

## Vector Database

In generative AI settings a Vector Database acts as the memory for
running agents.

Embeddings are efficiently indexed in a way to increase performance,
accuracy, and relevance of data & query processed by the LLM.

Examples: Chroma, Pinecone

In our demo architecture we use Chroma.

## Agent System

The Agent System is where our interaction with our application, the
Vector Datastore & the LLM occur.

We use Apache Karaf with a Backend-For-Frontend design to provide a web
interface, and integration to LocalAI via LangChain4j.

# The Result

Once this pipeline is created, an agent may use the pre-populated
embedded store for the LLM.

# Demo

# Conclusion

# About the Authors

[Jamie
Goodyear](https://github.com/savoirtech/blogs/blob/main/authors/JamieGoodyear.md)

# Reaching Out

Please do not hesitate to reach out with questions and comments, here on
the Blog, or through the Savoir Technologies website at
<https://www.savoirtech.com>.

# With Thanks

Thank you to the JavaFaker, Apache ActiveMQ, Apache Camel, Apache Karaf,
Apache CXF, LangChain4j, and LocalAI communities.

\(c\) 2024 Savoir Technologies
