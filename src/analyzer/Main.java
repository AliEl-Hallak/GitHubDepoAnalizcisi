/**
 * 
 * @author Ali El HALLAK  ali.hallak@ogr.sakarya.edu.tr
 * @since 10.03.2024
 * <p>
 * Sınıf ile ilgili açıklama:
 * 
 * kullanıcıdan alınan GitHub deposu URL'sini kullanarak depoyu klonlayan
 * ve içindeki Java dosyalarını analiz eden bir analizci sınıfıdır.
 * Klonlama işlemi başarılıysa, dosyaları analiz eder ve her bir dosyanın içerdiği
 * Javadoc satır sayısını, yorum satır sayısını, kod satır sayısını, toplam satır
 * sayısını ve fonksiyon sayısını hesaplar. Ayrıca, yorum sapma yüzdesini hesaplar
 * ve kullanıcıya sunar.
 * </p> 
 */

package analyzer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
public class Main {
	 public static void main(String[] args) {
	        Scanner scanner = new Scanner(System.in);
	        boolean basariliKlonlama = false;
	        
	        while (!basariliKlonlama) {
	            // Kullanıcıdan GitHub Depo URL'sini al
	            System.out.println("GitHub Depo URL'sini girin: ");
	            String repoUrl = scanner.nextLine();

	            // Benzersiz bir klasör adı oluştur
	            String benzersizKlasorAdi = "klonlanan_depo_" + System.currentTimeMillis();
	            // Klonlama işlemi için klasör yolunu oluştur
	            String klonKlasorYolu = Paths.get(System.getProperty("user.dir"), benzersizKlasorAdi).toString();

	            try {
	                // Klonlama için gerekli klasörü oluştur
	                Files.createDirectories(Paths.get(klonKlasorYolu));
	                // Klonlama komutunu oluştur ve çalıştır
	                String komut = "git clone " + repoUrl + " " + klonKlasorYolu;
	                Process islem = Runtime.getRuntime().exec(komut);
	                // Klonlama işlemi sırasında oluşan hataları oku
	                BufferedReader okuyucu = new BufferedReader(new InputStreamReader(islem.getErrorStream()));
	                StringBuilder hataCikti = new StringBuilder();
	                String satir;
	                while ((satir = okuyucu.readLine()) != null) {
	                    hataCikti.append(satir).append("\n");
	                }
	                // Klonlama işleminin çıkış değerini kontrol et
	                int cikisDegeri = islem.waitFor();
	                if (cikisDegeri == 0) {
	                    System.out.println("Klonlama işlemi tamamlandı. \n");
	                    basariliKlonlama = true;
	                    // Java dosyalarını analiz etmek için metodu çağır
	                    javaUzantiliDosyalariBul(klonKlasorYolu); 
	                } else {
	                    System.out.println("Hata: Girilen GitHub Depo URL'si yanlış veya erişilebilir değil. Lütfen doğru bir URL girin.");
	                    // Klonlama başarısız olduğunda oluşturulan dizini sil
	                    Files.delete(Paths.get(klonKlasorYolu)); 
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	            
	        }
	    }

	    // Java dosyalarını analiz etmek için metot
	    private static void javaUzantiliDosyalariBul(String klasorYolu) {
	        Path baslangicYolu = Paths.get(klasorYolu);
	        try {
	            // Belirtilen klasördeki tüm dosyaları ziyaret ederek Java dosyalarını analiz et
	            Files.walkFileTree(baslangicYolu, new SimpleFileVisitor<Path>() {
	                @Override
	                public FileVisitResult visitFile(Path dosya, BasicFileAttributes attrs) throws IOException {
	                    if (dosya.toString().endsWith(".java")) {
	                        // Java dosyasını analiz et
	                        dosyayiAnalizEt(dosya);
	                    }
	                    return FileVisitResult.CONTINUE;
	                }
	            });
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    
	    // Java dosyasını analiz etmek için metot
	    private static void dosyayiAnalizEt(Path dosya) throws IOException {
	        // Dosyanın içeriğini oku
	        String icerik = new String(Files.readAllBytes(dosya));
	        // Sınıf tanımlarını bulmak için kulanilan regex ifade
	        Pattern regexSinif = Pattern.compile("\\bclass\\s+\\w+");
	        Matcher eslestirici = regexSinif.matcher(icerik);

	        if (eslestirici.find()) { // Dosya sınıf tanımı içeriyorsa
	        	  List<String> tumSatirlar = Files.readAllLines(dosya);
	        	  long kodSatirSayisi = 0;
	        	  long toplamSatirSayisi = tumSatirlar.size();
	        	  long fonksiyonSayisi = 0;
	        	  long yorumSatirSayisi = 0; 
	        	  long javadocYorumSatirSayisi = 0; 
	        	    boolean blokYorumIcerisinde = false;
	        	    boolean javadocYorumIcerisinde = false;

	        	    Pattern regexFonksiyon = Pattern.compile("^\\s*(public|protected|private|static)\\s+.*\\(.*\\)\\s*\\{?\\s*$");
	        	    Pattern regexTekSatirYorum = Pattern.compile("//.*");
	        	    Pattern regexBlokVeyaJavadocYorumBaslangic = Pattern.compile("/\\*(\\*)?.*");
	        	    Pattern regexBlokVeyaJavadocYorumBitis = Pattern.compile(".*\\*/");
	        	    Pattern regexKodVeYorumIcerenSatir = Pattern.compile(".*;\\s*//.*");

	        	    for (String satir : tumSatirlar) {
	        	        // Javadoc veya blok yorum bloğu başlangıcı kontrolü
	        	        if (regexBlokVeyaJavadocYorumBaslangic.matcher(satir).find()) {
	        	            if (satir.trim().startsWith("/**")) {
	        	                javadocYorumIcerisinde = true;
	        	            } else {
	        	                blokYorumIcerisinde = true;
	        	            }
	        	            if (satir.contains("*/")) {
	        	                // Yorum bloğu aynı satırda başlayıp bitiyorsa, içerik olarak sayma
	        	                continue;
	        	            }
	        	        } else if ((blokYorumIcerisinde || javadocYorumIcerisinde) && regexBlokVeyaJavadocYorumBitis.matcher(satir).find()) {
	        	            blokYorumIcerisinde = false;
	        	            javadocYorumIcerisinde = false; 
	        	            continue; // Yorum bloğunun bitiş satırını sayma
	        	        } else if (blokYorumIcerisinde || javadocYorumIcerisinde) {
	        	            // Yorum bloğu içindeki içerik satırlarını say
	        	            if (javadocYorumIcerisinde) {
	        	                javadocYorumSatirSayisi++;
	        	            } else {
	        	                yorumSatirSayisi++;
	        	            }
	        	        } else if (regexTekSatirYorum.matcher(satir).find() || regexKodVeYorumIcerenSatir.matcher(satir).matches()) {
	        	            yorumSatirSayisi++;  // Tek satırlık yorumları say
	        	            if (regexKodVeYorumIcerenSatir.matcher(satir).matches()) {
	        	                kodSatirSayisi++; // Kod ve yorum içeren satırları kod satırı olarak say
	        	            }
	        	        } else if (!satir.trim().isEmpty()) {
	        	            kodSatirSayisi++; // Kod satırı olarak say
	        	        }

	        	        if (regexFonksiyon.matcher(satir).find()) {
	        	            fonksiyonSayisi++; // Fonksiyon sayisi

	        	        }
	        	    }
	        	    
	        	    // Sonuçları ekrana yazdır
	        	    System.out.println("-----------------------------------------");
	        	    System.out.println("Sınıf: " + dosya.getFileName());
	        	    System.out.println("Javadoc  Satır Sayısı: " + javadocYorumSatirSayisi);
	        	    System.out.println("Yorum Satır Sayısı: " + yorumSatirSayisi);
	        	    System.out.println("Kod Satır Sayısı: " + kodSatirSayisi);
	        	    System.out.println("LOC: " + toplamSatirSayisi);
	        	    System.out.println("Fonksiyon Sayısı: " + fonksiyonSayisi);
	                // Yorum sapma yüzdesini hesapla ve yazdır
	        	    yorumSapmaYuzdesiHesapla(javadocYorumSatirSayisi, yorumSatirSayisi,kodSatirSayisi,fonksiyonSayisi);
	        	}}
	    
	    // Yorum sapma yüzdesini hesaplamak için metot
	    public static void yorumSapmaYuzdesiHesapla(double javadocSatirSayisi, double yorumSatirSayisi, double kodSatirSayisi, double fonksiyonSayisi) {
	        double YG = ((javadocSatirSayisi + yorumSatirSayisi) * 0.8) / fonksiyonSayisi;
	        double YH = (kodSatirSayisi / fonksiyonSayisi) * 0.3;
	        double yorumSapmaYuzdesi = ((100 * YG) / YH) - 100;
	        DecimalFormatSymbols digerSemboller = new DecimalFormatSymbols(Locale.US);
	        DecimalFormat df = new DecimalFormat("#.00", digerSemboller);
	        String formatliSonuc = df.format(yorumSapmaYuzdesi);
	        System.out.println("Yorum Sapma Yüzdesi: % " + formatliSonuc);    }
	    
	}