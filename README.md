## Spring cloud stream with kafka

En este ejemplo vemos como usar spring cloud stream con kafka y avro, lo primero es arranca los containers de:

- kafka
- zookeeper
- schema registry

Solo tenemos que lanzar el siguiente comando para arrancar todos los contenedores:

```shell script
docker-compose up -d
```

Lo siguiente es generar los binarios de productor y consumidor

```shell script
mvn clean install
```

Una vez generado los binarios podemos atacar el api del productor:


> ### Api productor
>
> - Crear un coche
>
>```shell script
>curl --request POST \
>  --url http://localhost:9080/producer/car \
>  --header 'content-type: application/json' \
>  --data '{
>	"id" : "mazda",
>	"name": "mazda"
>}'
>```
>
> - Actualizar un coche
>
>```shell script
>curl --request PUT \
>  --url http://localhost:9080/producer/car \
>  --header 'content-type: application/json' \
>  --data '{
>	"id" : "mazda",
>	"name": "mazda3"
>}'
>```
>
> - Borrar un coche
>
>```shell script
>curl --request DELETE \
>  --url http://localhost:9080/producer/car/mazda \
>  --header 'content-type: application/json'
>```

-------

> ### Api consumidor
>
> - Obtener todos los coches
>
>```shell script
>curl --request GET \
>  --url http://localhost:8080/consumer
>``