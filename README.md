# Closed Caption Decoder

This project is broken down into the following main components:

1. SEI Nal unit - Where the closed captions are located and how the data is stored
2. tsDecoder - ignore the audio decoder the file it creates is too large. This is where the decoding process is
3. ASCII Table - used to decode the hex. 708 table is different from 608 since 608 contains odd parity
4. 608deocder - way to decode 608
5. 708deocder - way to decode 708 since it contains a lot of wiriting options compared to 608 that need to be differentiated. Those options have not been implemented yet.
