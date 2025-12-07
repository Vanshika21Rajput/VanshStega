package com.example.infosec.Controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@RestController
public class imagebind {



    private static char getLSB(int pixelValue) {

        int lsb = pixelValue & 1;  // Get last bit
        return (lsb == 1) ? '1' : '0';
    }

    private static int getPixelValue(BufferedImage image, int index) {
        int width = image.getWidth();
        int x = index % width;
        int y = index / width;

        if (y >= image.getHeight()) {
            throw new IllegalArgumentException("Pixel index out of bounds");
        }

        return image.getRGB(x, y);
    }


    public static int extractLength(BufferedImage image) {
        StringBuilder lengthBits = new StringBuilder();


        for (int i = 0; i < 16; i++) {
            int pixelValue = getPixelValue(image, i);
            lengthBits.append(getLSB(pixelValue));
        }

        return Integer.parseInt(lengthBits.toString(), 2);
    }

    @PostMapping("/extract")
    public static String methods(@RequestParam("image") MultipartFile f) throws IOException {
        StringBuilder binary = new StringBuilder();

        BufferedImage image = ImageIO.read(f.getInputStream());
        int l = extractLength(image)*8;
System.out.print("lengtho"+l);





        int skipped=0;
                outer:
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {

                        if (skipped < 16) {
                            skipped++;
                            continue;
                        }
                        if(l<=0)
                        {
                            break outer;
                        }

                        int rgb = image.getRGB(x, y);
                        System.out.println("rgb" + rgb);
                        int blue = rgb & 0xff;
                        int bit = blue & 1;

                        System.out.println("biary bit of message" + bit);
                        binary.append(bit);


                        if (binary.length() % 8 == 0) {
                            String byteStr = binary.substring(binary.length() - 8);
                            if (byteStr.equals("00000000")) {
                                break outer;
                            }
                        }
                        l--;
                    }
                }



        StringBuilder message = new StringBuilder();
        for (int i = 0; i < binary.length(); i += 8) {
            if (i + 8 > binary.length()) break;
            String byteStr = binary.substring(i, i + 8);
            if (byteStr.equals("00000000")) break;
            int charCode = Integer.parseInt(byteStr, 2);
            message.append((char) charCode);
        }
        System.out.print("stringdates" + message.toString());
        return message.toString();
    }


@GetMapping("/html")
        public static String meth()
{
    return  "van.html";

}



    @PostMapping("/hide")
    public static ResponseEntity<byte[]> method(@RequestParam("image") MultipartFile img, @RequestParam("data") String data) throws IOException {

        BufferedImage image = ImageIO.read(img.getInputStream());
        String binaryData = tobinary(data) + "00000000";

        int dataIndex = 0;

        outer:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (dataIndex >= binaryData.length()) {
                    break outer; // all data embedded
                }

                int rgb = image.getRGB(x, y);


                int red = (rgb >> 16) & 0xff;
                int green = (rgb >> 8) & 0xff;
                int blue = rgb & 0xff;


                int bit = binaryData.charAt(dataIndex) - '0';
                blue = (blue & 0xFE) | bit;
                int newRgb = (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newRgb);
                System.out.println("olf rgb"+rgb);
                System.out.println("new rgb"+newRgb);
                dataIndex++;

            }

        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
    public static String tobinary(String data) {

        StringBuilder  binary=new StringBuilder(String.format("%16s", Integer.toBinaryString(data.length())).replace(' ', '0'));
        for (char c : data.toCharArray()) {
            String binChar = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
            System.out.println(c + " = " + binChar);  // For debugging

            binary.append(binChar);
        }
        return binary.toString();
    }
//    public static void main(String[] args) throws IOException {
//        BufferedImage image = ImageIO.read(new File("C:/Users/HP/Downloads/Infosec/src/main/java/com/example/infosec/me.png"));
//        String secretData = "Hello World";
//
//        BufferedImage stegoImage = method(image, secretData);
//
//        ImageIO.write(stegoImage, "png", new File("output.png"));
//        System.out.println("Data hidden into image successfully.");
//        BufferedImage img=ImageIO.read(new File("C:/Users/HP/Downloads/Infosec/output.png"));
//        String mess=methods(img);
//        System.out.println("data is"+mess);
//    }

}

