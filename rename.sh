#!/bin/bash

a=$(date +"%T")
b=$RANDOM
c="${a}${b}"

for f in *.apk; do
    mv -- "$f" "${c}.apk"
done

# cd $1
# for f in *; do 
# # mv  "$f" "${f%.*}.png"
# mv  "$f" "${c}.png"
# done

# for /Users/leon/desktop in *.png; do mv /Users/leon/desktop ${c}.png; done
# for i in *.png; do mv $i ${c}.png; done
echo "${c}"