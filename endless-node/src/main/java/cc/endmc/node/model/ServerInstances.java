package cc.endmc.node.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ServerInstances implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String instanceName;
    private String version;
    private String coreType;
    private String filePath;
    private String status;
    private Integer port;
    private String jvmArgs;
    private Integer memoryMb;
    private Integer createdBy;
    private Date createdAt;
    private Date updatedAt;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        ServerInstances other = (ServerInstances) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getInstanceName() == null ? other.getInstanceName() == null : this.getInstanceName().equals(other.getInstanceName()))
                && (this.getVersion() == null ? other.getVersion() == null : this.getVersion().equals(other.getVersion()))
                && (this.getCoreType() == null ? other.getCoreType() == null : this.getCoreType().equals(other.getCoreType()))
                && (this.getFilePath() == null ? other.getFilePath() == null : this.getFilePath().equals(other.getFilePath()))
                && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
                && (this.getPort() == null ? other.getPort() == null : this.getPort().equals(other.getPort()))
                && (this.getJvmArgs() == null ? other.getJvmArgs() == null : this.getJvmArgs().equals(other.getJvmArgs()))
                && (this.getMemoryMb() == null ? other.getMemoryMb() == null : this.getMemoryMb().equals(other.getMemoryMb()))
                && (this.getCreatedBy() == null ? other.getCreatedBy() == null : this.getCreatedBy().equals(other.getCreatedBy()))
                && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
                && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getInstanceName() == null) ? 0 : getInstanceName().hashCode());
        result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
        result = prime * result + ((getCoreType() == null) ? 0 : getCoreType().hashCode());
        result = prime * result + ((getFilePath() == null) ? 0 : getFilePath().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getPort() == null) ? 0 : getPort().hashCode());
        result = prime * result + ((getJvmArgs() == null) ? 0 : getJvmArgs().hashCode());
        result = prime * result + ((getMemoryMb() == null) ? 0 : getMemoryMb().hashCode());
        result = prime * result + ((getCreatedBy() == null) ? 0 : getCreatedBy().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", instanceName=").append(instanceName);
        sb.append(", version=").append(version);
        sb.append(", coreType=").append(coreType);
        sb.append(", filePath=").append(filePath);
        sb.append(", status=").append(status);
        sb.append(", port=").append(port);
        sb.append(", jvmArgs=").append(jvmArgs);
        sb.append(", memoryMb=").append(memoryMb);
        sb.append(", createdBy=").append(createdBy);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}