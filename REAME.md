Setting up this very  very basic example:

```bash
podman volume create neo4jdata
podman run --rm -v neo4jdata:/data --name neo4j --publish=7474:7474 --publish=7687:7687 -e NEO4J_AUTH=neo4j/neo4jpassword -e NEO4J_PLUGINS=\[\"apoc\"\] neo4j:5.25.1
```

You need to use Java 21
```bash
git clone https://github.com/model-graph-tools/analyzer.git
mvn clean install
```
Start a local instance of WildFly
Then execute tthis command to fill the Neo4J Database
```bash
java -jar ./target/model-graph-analyzer-0.0.1.jar -v -s neo4j -t neo4jpassword -c /
```

You should be able to browser some content using http://localhost:7474/browser/


For Ollama, you need to have Ollama running and use the model "tomasonjo/llama3-text2cypher-demo".


Now you an play with the graphrag example.




