/*
 *  MIT License
 *
 *  Copyright (c) 2016  Altisource
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
PrimeFaces.widget.Cytoscape = PrimeFaces.widget.DeferredWidget.extend({

    init: function(cfg) {
        this._super(cfg);

        this.renderDeferred();
    },

    _render: function() {
        var $this = this;

        $this.cfg.ready = function(){
            $this.cyObj = this;
            $this.bindEvents();
        };

        $(PrimeFaces.escapeClientId($this.jq.attr('id'))).cytoscape($this.cfg);
    },

    bindEvents: function() {
        var $this = this;

        $this.cyObj.on('cxttap', 'node', undefined, function (event) {
            var node = this;
            if (node.hasClass("labelHolder")) {
                node = node.parent();
            }
            if ($this.cyObj.scratch(node.id() + "_removed")) {
                $this.cyObj.scratch(node.id() + "_removed").restore();
                $this.cyObj.removeScratch(node.id() + "_removed");
            } else if (node.isParent()) {
                var toRemove = node.children().descendants();
                $this.cyObj.scratch(node.id() + "_removed", toRemove.union(toRemove.connectedEdges()).remove());
            }
            $this.cyObj.layout();
        });
        $this.cyObj.on('select', 'node', undefined, function (event) {
            var node = this;
            if (node.hasClass("labelHolder")) {
                node.unselect();
                node = node.parent();
                if (node.selected()) {
                    node.unselect();
                } else {
                    node.select();
                }
            }
            $this.onSelectNode(node);
        });
        $this.cyObj.on('unselect', 'node', undefined, function (event) {
            var node = this;
            $this.onUnselectNode(node);
        });
    },

    onSelectNode: function(node) {
        var options = {
            source: this.id,
            process: this.id,
            params: [
                {name: this.id + '_selectNode', value: true},
                {name: this.id + '_itemId', value: node.id()}
            ]
        };

        if(this.hasBehavior('selectNode')) {
            var behavior = this.cfg.behaviors['selectNode'];

            behavior.call(this, options);
        }
        else {
            PrimeFaces.ajax.Request.handle(options);
        }
    },

    onUnselectNode: function(node) {
        var options = {
            source: this.id,
            process: this.id,
            params: [
                {name: this.id + '_unselectNode', value: true},
                {name: this.id + '_itemId', value: node.id()}
            ]
        };

        if(this.hasBehavior('unselectNode')) {
            var behavior = this.cfg.behaviors['unselectNode'];

            behavior.call(this, options);
        }
        else {
            PrimeFaces.ajax.Request.handle(options);
        }
    },

    hasBehavior: function(event) {
        if(this.cfg.behaviors) {
            return this.cfg.behaviors[event] != undefined;
        }

        return false;
    },

    synchronizeClientSide: function(synchData) {
        for(var i=0; i < synchData.length; i++) {
            var synchNode = synchData[i];
            var node = this.cyObj.getElementById(synchNode.data.id);
            if (node.inside() && node.json().classes != synchNode.classes) {
                node.classes(synchNode.classes);
            }
        }
    },

    synchronize: function() {
        var options = {
            source: this.id,
            update: this.cfg.update,
            process: this.id,
            formId: this.cfg.formId,
            params: [
                {name: this.id + '_synchModel', value: true}
            ]
        };

        var $this = this;

        options.onsuccess = function(responseXML) {
            //receive data for client side model synchronization
            var xmlDoc = $(responseXML.documentElement);
            var callbackParams = xmlDoc.find('extension[ln="primefaces"][type="args"]');
            var params = callbackParams.length > 0 ? $.parseJSON(callbackParams.text()) : {};
            $this.synchronizeClientSide($.parseJSON(params[$this.id + "_synchModel"]));

            //process updates
            updates = xmlDoc.find("update");
            for(var i=0; i < updates.length; i++) {
                var update = updates.eq(i);
                PrimeFaces.ajax.Utils.updateElement.call(this, update.attr('id'), update.text());
            }

            PrimeFaces.ajax.Response.handle(responseXML);

            return true;
        };

        PrimeFaces.ajax.AjaxRequest(options);
    },

    doLayout: function() {
        this.cyObj.layout();
    }

});
