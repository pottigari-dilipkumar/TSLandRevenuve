package in.gov.landrevenue.blockchain;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "landregistry.blockchain")
public class BlockchainProperties {
    private boolean enabled;
    private String rpcUrl;
    private Long chainId;
    private String contractAddress;
    private String registrarPrivateKey;
    private Long gasLimit = 250000L;
    private Long pollingIntervalMs = 1500L;
    private Integer receiptAttempts = 40;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRpcUrl() {
        return rpcUrl;
    }

    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = rpcUrl;
    }

    public Long getChainId() {
        return chainId;
    }

    public void setChainId(Long chainId) {
        this.chainId = chainId;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getRegistrarPrivateKey() {
        return registrarPrivateKey;
    }

    public void setRegistrarPrivateKey(String registrarPrivateKey) {
        this.registrarPrivateKey = registrarPrivateKey;
    }

    public Long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(Long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public Long getPollingIntervalMs() {
        return pollingIntervalMs;
    }

    public void setPollingIntervalMs(Long pollingIntervalMs) {
        this.pollingIntervalMs = pollingIntervalMs;
    }

    public Integer getReceiptAttempts() {
        return receiptAttempts;
    }

    public void setReceiptAttempts(Integer receiptAttempts) {
        this.receiptAttempts = receiptAttempts;
    }
}
