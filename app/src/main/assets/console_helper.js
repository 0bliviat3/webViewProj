window.__consoleHelper = {
    exec: function(code) {
        try {
            const result = window.eval(code);
            return JSON.stringify(this.serialize('', result));
        } catch(e) {
            return JSON.stringify({ type: "error", value: e.toString() });
        }
    },

    serialize: function(key, obj) {
        if(obj === null) return { key, value: "null", type: "null" };
        if(obj === undefined) return { key, value: "undefined", type: "undefined" };
        if(typeof obj === "function") return { key, value: obj.toString(), type: "function" };
        if(typeof obj !== "object") return { key, value: obj.toString(), type: typeof obj };

        const children = {};
        try {
            for(const k in obj){
                try {
                    const v = obj[k];
                    children[k] = this.serialize(k, v);
                } catch(e) {
                    children[k] = { key: k, value: "[unserializable]", type: "error" };
                }
            }
        } catch(e) {
            return { key, value: "[unserializable object]", type: "error" };
        }

        return { key, children, type: Array.isArray(obj) ? "array" : "object" };
    }
};
