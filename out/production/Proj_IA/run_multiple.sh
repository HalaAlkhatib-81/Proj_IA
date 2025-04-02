#!/bin/bash

# Archivo para almacenar todos los resultados
output_file="resultados.txt"

# Limpiar el archivo de resultados si existe
> "$output_file"

# Bucle para ejecutar 10 veces
for i in {0..9}
do
    echo "Ejecución $((i+1))" >> "$output_file"
    echo "----------------------------------------" >> "$output_file"

    # Calcular y correspondiente (y = i + 1)
    y=$((i+1))

    # Crear archivo temporal con las entradas
    echo "100" > input.txt        # nsensores
    echo "4" >> input.txt       # ncentros
    echo "1" >> input.txt       # Hill Climbing
    echo "0" >> input.txt       # Semillas no random
    echo "$i" >> input.txt      # semillaSensores (x)
    echo "$y" >> input.txt      # semillaCentros (y)
    echo "1" >> input.txt       # Greedy
    echo "0" >> input.txt       # Heurística con coste + info

    # Ejecutar el programa con las entradas y añadir salida al archivo de resultados
    java Main < input.txt >> "$output_file" 2>&1

    # Añadir separador entre ejecuciones
    echo -e "\n\n" >> "$output_file"

    # Limpiar archivo temporal
    rm input.txt
done

echo "Ejecuciones completadas. Resultados guardados en $output_file"