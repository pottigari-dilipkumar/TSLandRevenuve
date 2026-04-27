package in.gov.landrevenue.blockchain;

import in.gov.landrevenue.model.RegistrationRecordEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class BlockchainRegistrationGateway {
    private final BlockchainProperties properties;

    public BlockchainRegistrationGateway(BlockchainProperties properties) {
        this.properties = properties;
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public BlockchainAnchorResult anchorRegistration(RegistrationRecordEntity entity) {
        if (!properties.isEnabled()) {
            return BlockchainAnchorResult.skipped();
        }

        if (!StringUtils.hasText(entity.getOwnerWalletAddress())) {
            return BlockchainAnchorResult.failed("Owner wallet address is required for on-chain anchoring");
        }
        if (!StringUtils.hasText(properties.getRpcUrl())
                || !StringUtils.hasText(properties.getContractAddress())
                || !StringUtils.hasText(properties.getRegistrarPrivateKey())
                || properties.getChainId() == null) {
            return BlockchainAnchorResult.failed("Blockchain configuration is incomplete");
        }

        Web3j web3j = Web3j.build(new HttpService(properties.getRpcUrl()));
        try {
            Credentials credentials = Credentials.create(properties.getRegistrarPrivateKey());
            RawTransactionManager txManager = new RawTransactionManager(
                    web3j,
                    credentials,
                    properties.getChainId()
            );

            List<Type> args = new ArrayList<>();
            args.add(new Bytes32(toBytes32(entity.getParcelId())));
            args.add(new Address(entity.getOwnerWalletAddress()));
            args.add(new Bytes32(toBytes32(entity.getDeedHash())));
            args.add(new Utf8String(entity.getRegistrationRef()));

            Function function = new Function("registerLand", args, List.of());
            String encodedFunction = FunctionEncoder.encode(function);

            EthGasPrice gasPriceResponse = web3j.ethGasPrice().send();
            BigInteger gasPrice = gasPriceResponse.getGasPrice();

            EthSendTransaction txResponse = txManager.sendTransaction(
                    gasPrice,
                    BigInteger.valueOf(properties.getGasLimit()),
                    properties.getContractAddress(),
                    encodedFunction,
                    BigInteger.ZERO
            );

            if (txResponse.hasError()) {
                return BlockchainAnchorResult.failed(txResponse.getError().getMessage());
            }

            PollingTransactionReceiptProcessor processor = new PollingTransactionReceiptProcessor(
                    web3j,
                    properties.getPollingIntervalMs(),
                    properties.getReceiptAttempts()
            );
            TransactionReceipt receipt = processor.waitForTransactionReceipt(txResponse.getTransactionHash());
            if (!"0x1".equalsIgnoreCase(receipt.getStatus())) {
                return BlockchainAnchorResult.failed("Transaction reverted on chain");
            }

            return BlockchainAnchorResult.synced(
                    receipt.getTransactionHash(),
                    receipt.getBlockNumber().longValue(),
                    Instant.now()
            );
        } catch (Exception ex) {
            return BlockchainAnchorResult.failed(ex.getMessage());
        } finally {
            web3j.shutdown();
        }
    }

    /**
     * Anchors a clean-architecture LandRegistration onto the chain.
     * Uses the SHA3 hash of the registrationRef as parcelId and deedHash.
     * Uses a system-level placeholder address since citizens don't have Ethereum wallets.
     */
    public BlockchainAnchorResult anchorLandRegistration(String registrationRef, String surveyNumber, String district) {
        if (!properties.isEnabled()) {
            return BlockchainAnchorResult.skipped();
        }
        RegistrationRecordEntity synthetic = new RegistrationRecordEntity();
        synthetic.setRegistrationRef(registrationRef);
        synthetic.setParcelId(surveyNumber + ":" + district);
        synthetic.setDeedHash(Hash.sha3String(registrationRef));
        // System placeholder — real deployments should map citizen identity to an on-chain address
        synthetic.setOwnerWalletAddress("0x0000000000000000000000000000000000000001");
        return anchorRegistration(synthetic);
    }

    public boolean isHealthy() {
        if (!properties.isEnabled()) {
            return false;
        }

        Web3j web3j = Web3j.build(new HttpService(properties.getRpcUrl()));
        try {
            web3j.ethBlockNumber().send();
            web3j.ethGetBalance(properties.getContractAddress(), DefaultBlockParameterName.LATEST).send();
            return true;
        } catch (Exception ex) {
            return false;
        } finally {
            web3j.shutdown();
        }
    }

    private byte[] toBytes32(String rawValue) {
        String sanitized = rawValue == null ? "" : rawValue.trim();
        byte[] bytes;

        if (Numeric.containsHexPrefix(sanitized)) {
            bytes = Numeric.hexStringToByteArray(sanitized);
        } else {
            bytes = Numeric.hexStringToByteArray(Hash.sha3String(sanitized));
        }

        byte[] output = new byte[32];
        int copyLength = Math.min(bytes.length, 32);
        System.arraycopy(bytes, 0, output, 0, copyLength);
        return output;
    }
}
