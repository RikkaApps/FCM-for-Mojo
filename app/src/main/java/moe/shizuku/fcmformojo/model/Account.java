package moe.shizuku.fcmformojo.model;


import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

@Keep
public class Account {

    private String account;
    @SerializedName("passwd")
    private Password password;

    public Account(String account, Password password) {
        this.account = account;
        this.password = password;
    }

    public String getAccount() {
        return account;
    }

    public @NonNull Password getPassword() {
        return password == null ? new Password(null, null) : password;
    }

    @Keep
    public static class Password {

        private String raw;
        private String md5;

        public Password(String raw, String md5) {
            this.raw = raw;
            this.md5 = md5;
        }

        public String getRaw() {
            return raw;
        }

        public String getMd5() {
            return md5;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Password account = (Password) o;

            if (raw != null ? !raw.equals(account.raw) : account.raw != null) return false;
            return md5 != null ? md5.equals(account.md5) : account.md5 == null;
        }

        @Override
        public int hashCode() {
            int result = raw != null ? raw.hashCode() : 0;
            result = 31 * result + (md5 != null ? md5.hashCode() : 0);
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account1 = (Account) o;
        return Objects.equals(account, account1.account) &&
                Objects.equals(password, account1.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, password);
    }
}
