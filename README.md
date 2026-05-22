# Multidisci Audiometer Uygulaması

Bu proje, bir odyometre cihazı ile seri port üzerinden iletişim kurarak işitme testi yürütmeye yönelik bir Java Swing uygulamasıdır. Uygulama, Hughson-Westlake algoritması üzerinden yanıtları işleyerek frekans bazında işitme eşiği sonuçlarını görselleştirir.

## Proje Yapısı

- `pom.xml`: Maven proje tanımı, Java 17 derlemesi ve bağımlılıklar.
- `src/main/java/org/example/AudiometerGUI.java`: Ana Swing GUI sınıfı. Seri port bağlantısı, sinyal gönderme, hasta yanıtlarını alma, grafik kaydetme işlemlerini içerir.
- `src/main/java/org/example/AudiogramPanel.java`: Odyogram grafiğini çizmek için özel JPanel.
- `src/main/java/org/example/fp/HughsonWestlakeAlgorithm.java`: Hughson-Westlake işitme testi algoritması.
- `src/main/java/org/example/fp/AudiometryTestState.java`: Test durumu verisini saklayan kayıt sınıfı.
- `src/main/java/org/example/fp/Ear.java`: Sağ/Sol kulak seçimi için enum.
- `src/main/java/org/example/fp/PatientResponse.java`: Hasta yanıtlarını temsil eden enum.
- `src/main/java/org/example/fp/ResponseParser.java`: Seri porttan gelen metin tabanlı yanıtları ayrıştırır.
- `src/main/java/org/example/fp/ThresholdResult.java`: Eşik sonucu bilgisini saklar.
- `src/main/java/org/example/fp/Trial.java`: Bir deneme kaydını tutar.

## Nasıl Çalışır

1. Uygulamayı başlattığınızda, GUI açılır ve mevcut seri portlar listesinden bir port seçebilirsiniz.
2. `Bağlan` düğmesi cihazla seri port bağlantısı açar.
3. `Frekans` ve `Şiddet` seçimi yaparak `Sinyal Gönder` ile cihaza test sinyali gönderirsiniz.
4. Cihazdan gelen yanıtlar `ResponseParser` ile `HEARD` / `NOT_HEARD` olarak ayrıştırılır.
5. `HughsonWestlakeAlgorithm` her yanıtı işleyerek test durumunu günceller. Eşik bulunduğunda sonuç grafiğe eklenir.
6. `Grafiği Kaydet` seçeneği ile odayogram ekran görüntüsü PNG olarak kaydedilir.

## Gereksinimler

- Java 17
- Maven
- Seri port cihazı çalışır durumda ve uygun sürücü yüklü olmalı.

## Çalıştırma

Terminalden proje kök dizininde aşağıdaki komutları kullanabilirsiniz:

```bash
mvn compile
mvn exec:java -Dexec.mainClass="org.example.AudiometerGUI"
```

Not: `exec:java` çalışması için ek Maven `exec-maven-plugin` tanımı gerekebilir. Şu anda proje `pom.xml` içinde bu eklenti yok, bu nedenle çalıştırma için IDE üzerinden `AudiometerGUI.main()` doğrudan başlatmak daha kolay olabilir.

### Alternatif: IDE ile çalıştırma

1. Projeyi IDE'ye (IntelliJ IDEA, Eclipse, VS Code) açın.
2. JDK 17 olarak yapılandırın.
3. `org.example.AudiometerGUI` sınıfında `main` metodunu çalıştırın.

## Testler

Projede JUnit 5 ve jqwik test bağımlılıkları mevcut.

Aşağıdaki komut ile testleri çalıştırabilirsiniz:

```bash
mvn test
```

## Önemli Notlar

- Uygulama seri port üzerinden gelen satır sonlu (`\n`) mesajları bekler.
- Geçerli yanıtlar: `RESPONSE`, `HEARD`, `YES`, `NO_RESPONSE`, `NOT_HEARD`, `NO`.
- Eşik hesaplama, aynı şiddet seviyesinde en az iki `HEARD` yanıtı olduğunda tamamlanır.

## Geliştirme Notları

- `AudiometerGUI` bildirimleri Swing `JOptionPane` ile gösterir.
- `AudiogramPanel` sağ kulak için kırmızı `O`, sol kulak için mavi `X` çizimi yapar.
- `HughsonWestlakeAlgorithm` işitme testi mantığını, seviyesi `-10` ila `120` dB arasında `5` dB artış/azalış aralığıyla uygular.

---

Hazır bir çalıştırma kılavuzu olarak bu dosyayı güncelleyebilir veya `pom.xml` içine `exec-maven-plugin` ekleyerek CLI tabanlı başlatmayı kolaylaştırabilirsiniz.


## Algoritma Kuralları (BME Kural Seti — IEC 60645-1 / ISO 8253-1)

Algoritma, biyomedikal ekibinin teslim ettiği kural setine göre uygulanmıştır:
- HEARD sonrası: -10 dB; NOT_HEARD sonrası: +5 dB.
- Başlangıç şiddeti: 40 dB HL (kural setinin verdiği 30-40 dB aralığında).
- Eşik kriteri: aynı seviyede en az 2 ASCENDING yanıtın HEARD olduğu en düşük şiddet
  (2-of-3 ascending). Descending yanıtlar eşik sayımına dahil edilmez.
- Şiddet aralığı: [-10, 120] dB HL, 5 dB adımlarla.