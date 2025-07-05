import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Logger;

public class CPABE {
    private static final Logger logger = Logger.getLogger(CPABE.class.getName());

    private static Pairing pairing;

    public static Pairing getPairing(String pairingParamsFileName){
        if(pairing == null){
            pairing = PairingFactory.getPairing(pairingParamsFileName);
        }
        return pairing;
    }

    public static void setup(String pairingParamsFileName, String pkFileName, String mskFileName){
//        pairing = getPairing(pairingParamsFileName);
        Element g = pairing.getG1().newRandomElement().getImmutable();
        Element beta  = pairing.getZr().newRandomElement().getImmutable();
        Element alpha = pairing.getZr().newRandomElement().getImmutable();

        Element inverseBeta = beta.invert().getImmutable();//求β的逆元
        //计算PK包含的元素
        Element h = g.duplicate().powZn(beta);
        Element f = g.duplicate().powZn(inverseBeta);
        Element Y = pairing.pairing(g,g).powZn(alpha).getImmutable();

        //计算MSK包含的元素
        Element g_a = g.duplicate().powZn(alpha);

        //存入文件中
        Properties pkProps = new Properties();
        Properties mskProps = new Properties();
        pkProps.setProperty("h", Base64.getEncoder().withoutPadding().encodeToString(h.toBytes()));
        pkProps.setProperty("f", Base64.getEncoder().withoutPadding().encodeToString(f.toBytes()));
        pkProps.setProperty("Y", Base64.getEncoder().withoutPadding().encodeToString(Y.toBytes()));
        mskProps.setProperty("beta", Base64.getEncoder().withoutPadding().encodeToString(beta.toBytes()));
        mskProps.setProperty("g_a", Base64.getEncoder().withoutPadding().encodeToString(g_a.toBytes()));

        storePropToFile(pkProps, pkFileName);
        storePropToFile(mskProps, mskFileName);
    }

    public static void keygen(String pairingParamsFileName, int[] userAttList, String pkFileName, String mskFileName, String skFileName){
        Element r = pairing.getZr().newRandomElement().getImmutable();
        Properties pkProps = loadPropFromFile(pkFileName);

        for(int att: userAttList){

        }
    }

    public static void storePropToFile(Properties prop, String fileName) {
        try {
            Path path = Paths.get(fileName);
            Path parentDir = path.getParent();

            // 确保父目录存在
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
                System.out.println("✅ 创建目录：" + parentDir);
            }

            try (OutputStream outputStream = Files.newOutputStream(path)) {
                prop.store(outputStream, "System Parameters");
                logger.info("✅ 文件保存成功: " + path.toAbsolutePath());//日志就会写入 logs/kpabe.log 文件，不再输出到控制台
//                System.out.println("✅ 文件保存成功：" + path); // 添加成功确认
            }
        } catch (IOException e) {
            System.err.println("❌ 保存失败：" + fileName);
            throw new RuntimeException("保存失败: " + e.getMessage(), e);
        }
    }

    public static Properties loadPropFromFile(String fileName) {
        if(!Files.exists(Paths.get(fileName))){
            throw new IllegalArgumentException("文件不存在" + fileName);
        }
        Properties prop = new Properties();
        try(FileInputStream inputStream = new FileInputStream(fileName)){
            prop.load(inputStream);
        }catch (IOException e){
            System.err.println("加载文件失败" + fileName);
            System.err.println("错误原因" + e.getMessage());
            throw new RuntimeException("无法加载配置文件" + fileName, e);
        }
        return prop;
    }

}
