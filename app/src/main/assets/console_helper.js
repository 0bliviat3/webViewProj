window.__consoleHelper = {
    MAX_DEPTH: 2,
    MAX_KEYS: 50,

    exec: function(code) {
        try {
            const result = window.eval(code);
            return JSON.stringify(this.serialize('', result, 0));
        } catch(e) {
            return JSON.stringify({ type: "error", value: e.toString() });
        }
    },

    serialize: function(key, obj, depth) {
        if(obj === null) return { key, value: "null", type: "null" };
        if(obj === undefined) return { key, value: "undefined", type: "undefined" };
        if(typeof obj === "function") return { key, value: obj.toString(), type: "function" };
        if(typeof obj !== "object") return { key, value: obj.toString(), type: typeof obj };

        // 깊이 제한 초과 시
        if (depth > this.MAX_DEPTH) {
            return { key, value: "[Max depth reached]", type: "object" };
        }

        const children = {};
        let count = 0;

        try {
            for(const k in obj){

                if (!Object.prototype.hasOwnProperty.call(obj, k)) continue;

                if (count >= this.MAX_KEYS) {
                    children["..."] = { key: "...", value: `[Only first ${this.MAX_KEYS} keys shown]`, type: "info" };
                    break;
                }

                try {
                    const v = obj[k];
                    children[k] = this.serialize(k, v, depth + 1);
                } catch(e) {
                    children[k] = { key: k, value: "[unserializable]", type: "error" };
                }
                count++;
            }
        } catch(e) {
            return { key, value: "[unserializable object]", type: "error" };
        }

        return { key, children, type: Array.isArray(obj) ? "array" : "object" };
    }
};
