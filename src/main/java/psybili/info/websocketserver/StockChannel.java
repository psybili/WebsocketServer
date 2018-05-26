package psybili.info.websocketserver;

public class StockChannel {
        String isin;
        boolean enabled;

        public StockChannel(String isin) {
            this.isin = isin;
            this.enabled = true;
        }

        public String getIsin() {
            return isin;
        }

        public void setIsin(String isin) {
            this.isin = isin;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }